package data_access.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;
import use_case.view_calendar.PublicHolidayGateway.PublicHoliday;

/**
 * Tests the Nager.Date gateway against a local HTTP server (no internet needed).
 * The JSON below is trimmed from the real 2026/CA response.
 */
class NagerPublicHolidayGatewayTest {

    private static final String CANADA_2026 = "["
            + "{\"date\":\"2026-01-01\",\"localName\":\"New Year's Day\",\"name\":\"New Year's Day\","
            + "\"global\":true,\"counties\":null},"
            + "{\"date\":\"2026-02-16\",\"localName\":\"Louis Riel Day\",\"name\":\"Louis Riel Day\","
            + "\"global\":false,\"counties\":[\"CA-MB\"]},"
            + "{\"date\":\"2026-02-16\",\"localName\":\"Family Day\",\"name\":\"Family Day\","
            + "\"global\":false,\"counties\":[\"CA-AB\",\"CA-BC\",\"CA-NB\",\"CA-ON\",\"CA-SK\"]},"
            + "{\"date\":\"2026-06-24\",\"localName\":\"Fête nationale du Québec\",\"name\":\"National Holiday\","
            + "\"global\":false,\"counties\":[\"CA-QC\"]},"
            + "{\"date\":\"not-a-date\",\"localName\":\"Broken\",\"name\":\"Broken\",\"global\":true},"
            + "{\"date\":\"2026-12-26\",\"localName\":\"Boxing Day\",\"name\":\"St. Stephen's Day\","
            + "\"global\":false,\"counties\":[\"CA-AB\",\"CA-NB\",\"CA-NS\",\"CA-ON\",\"CA-PE\"]}"
            + "]";

    private final AtomicInteger requests = new AtomicInteger();
    private final List<String> paths = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private int status = 200;
    private String body = CANADA_2026;

    private NagerPublicHolidayGateway startGateway() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            paths.add(exchange.getRequestURI().getPath());
            final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            if (status == 204) {
                exchange.sendResponseHeaders(status, -1);
            }
            else {
                exchange.sendResponseHeaders(status, bytes.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            }
            exchange.close();
        });
        server.start();
        return new NagerPublicHolidayGateway(
                "http://127.0.0.1:" + server.getAddress().getPort() + "/api/v3/PublicHolidays",
                NagerPublicHolidayGateway.DEFAULT_PROVINCE);
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static List<String> names(List<PublicHoliday> holidays) {
        final List<String> names = new ArrayList<>();
        for (PublicHoliday holiday : holidays) {
            names.add(holiday.getLocalName());
        }
        return names;
    }

    @Test
    void keepsNationwideAndOntarioHolidaysOnly() throws IOException {
        final List<PublicHoliday> holidays = startGateway().fetchPublicHolidays(2026, "CA");

        assertEquals(List.of("/api/v3/PublicHolidays/2026/CA"), paths);
        assertEquals(List.of("New Year's Day", "Family Day", "Boxing Day"), names(holidays));
        assertEquals(LocalDate.of(2026, 2, 16), holidays.get(1).getDate());
        assertEquals("St. Stephen's Day", holidays.get(2).getName());
    }

    @Test
    void countryCodeIsNormalized() throws IOException {
        startGateway().fetchPublicHolidays(2026, " ca ");

        assertEquals(List.of("/api/v3/PublicHolidays/2026/CA"), paths);
    }

    @Test
    void secondRequestForTheSameYearUsesTheCache() throws IOException {
        final NagerPublicHolidayGateway gateway = startGateway();

        gateway.fetchPublicHolidays(2026, "CA");
        final List<PublicHoliday> again = gateway.fetchPublicHolidays(2026, "CA");

        assertEquals(1, requests.get());
        assertEquals(3, again.size());
    }

    @Test
    void serverErrorGivesNoHolidaysAndIsRetriedLater() throws IOException {
        status = 500;
        final NagerPublicHolidayGateway gateway = startGateway();

        assertTrue(gateway.fetchPublicHolidays(2026, "CA").isEmpty());

        status = 200;
        assertEquals(3, gateway.fetchPublicHolidays(2026, "CA").size());
        assertEquals(2, requests.get());
    }

    @Test
    void noContentGivesNoHolidays() throws IOException {
        status = 204;

        assertTrue(startGateway().fetchPublicHolidays(2026, "ZZ").isEmpty());
    }

    @Test
    void malformedJsonGivesNoHolidays() throws IOException {
        body = "{\"oops\": ";

        assertTrue(startGateway().fetchPublicHolidays(2026, "CA").isEmpty());
    }

    @Test
    void unreachableServerGivesNoHolidays() throws IOException {
        final NagerPublicHolidayGateway gateway = startGateway();
        server.stop(0);
        server = null;

        assertTrue(gateway.fetchPublicHolidays(2026, "CA").isEmpty());
    }
}
