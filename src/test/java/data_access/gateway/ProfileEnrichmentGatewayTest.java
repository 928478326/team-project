package data_access.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import entity.LlmPersona;
import entity.WeekendStyle;
import okhttp3.OkHttpClient;
import use_case.edit_profile.LlmProfileEnrichmentGateway;
import use_case.edit_profile.LlmProfileEnrichmentGateway.SanitizedProfileSnapshot;

/** Exercises the HTTP contract against a loopback Gemini fixture. */
class ProfileEnrichmentGatewayTest {
    private static final int OK = 200;
    private static final int UNAVAILABLE = 503;
    private static final String GENERATED = "Enjoys walks and quiet study groups.";
    private final AtomicInteger hits = new AtomicInteger();
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> key = new AtomicReference<>();
    private final SanitizedProfileSnapshot profile = new SanitizedProfileSnapshot(
            WeekendStyle.STUDY_FOCUS, Set.of("study", "food", "private@example.invalid"));
    private final RuleBasedProfileEnrichmentGateway offline = new RuleBasedProfileEnrichmentGateway();
    private final OkHttpClient client = new OkHttpClient.Builder().callTimeout(2, TimeUnit.SECONDS).build();
    private HttpServer server;
    private String endpoint;
    private final AtomicInteger status = new AtomicInteger(OK);
    private final AtomicReference<String> reply = new AtomicReference<>("{}");

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/generateContent", this::respond);
        server.start();
        endpoint = "http://127.0.0.1:" + server.getAddress().getPort() + "/generateContent";
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private void respond(HttpExchange exchange) throws IOException {
        try {
            hits.incrementAndGet();
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            key.set(exchange.getRequestMethod() + " " + exchange.getRequestHeaders().getFirst("x-goog-api-key"));
            final byte[] bytes = reply.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), bytes.length);
            exchange.getResponseBody().write(bytes);
        }
        finally {
            exchange.close();
        }
    }

    private LlmProfileEnrichmentGateway gateway(String apiKey) {
        return new HttpLlmProfileEnrichmentGateway(apiKey, endpoint, client, offline);
    }

    @Test
    void sendsOnlyApprovedCategoriesAndReadsTheCompletedAnswer() {
        reply.set("{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":["
                + "{\"thought\":true,\"text\":\"discard\"},{\"text\":\"" + GENERATED + "\"}]}}]}");
        assertEquals(GENERATED, gateway("test-key").enrichPersona(profile).getSummary());
        assertEquals("POST test-key", key.get());
        final JSONObject envelope = new JSONObject(requestBody.get());
        final JSONObject content = envelope.getJSONArray("contents").getJSONObject(0);
        final JSONObject data = new JSONObject(content.getJSONArray("parts").getJSONObject(0).getString("text"));
        assertEquals(Set.of("weekendStyleText", "interestedCommunityIds"), data.keySet());
        assertEquals("Study Focus", data.getString("weekendStyleText"));
        assertFalse(requestBody.get().contains("private@example.invalid"));
        assertEquals(profile.getInterestedCommunityIds(), Set.copyOf(data.getJSONArray("interestedCommunityIds")
                .toList()));
        assertFalse(requestBody.get().contains("test-key"));
    }

    @Test
    void noKeyUsesDeterministicLocalPreferencesWithoutContactingServer() {
        final LlmProfileEnrichmentGateway gateway = gateway("  ");
        final LlmPersona persona = gateway.enrichPersona(profile);
        assertEquals("Weekend preference: Study Focus. Communities: food, study",
                persona.getSummary());
        assertEquals(persona, gateway.enrichPersona(profile));
        assertEquals(0, hits.get());
    }

    @Test
    void fallsBackOnHttpErrorsAndUnusableResponses() {
        final LlmProfileEnrichmentGateway gateway = gateway("test-key");
        final LlmPersona expected = offline.enrichPersona(profile);
        status.set(UNAVAILABLE);
        assertEquals(expected, gateway.enrichPersona(profile));
        status.set(OK);
        for (String response : List.of("malformed", "{\"candidates\":[]}",
                "{\"candidates\":[{\"finishReason\":\"SAFETY\"}]}",
                "{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":[{\"text\":\" \"}]}}]}")) {
            reply.set(response);
            assertEquals(expected, gateway.enrichPersona(profile));
        }
    }

    @Test
    void connectionFailureAlsoUsesTheLocalGateway() {
        server.stop(0);
        assertEquals(offline.enrichPersona(profile), gateway("test-key").enrichPersona(profile));
    }

    @Test
    void customTextAndUnknownCommunitiesStayLocal() {
        final SanitizedProfileSnapshot preferences = new SanitizedProfileSnapshot(
                WeekendStyle.of("Contact me at private@example.invalid"), Set.of("private@example.invalid"));
        assertTrue(preferences.isEmpty());
        assertTrue(gateway("test-key").enrichPersona(preferences).isEmpty());
        assertEquals(0, hits.get());
    }
}
