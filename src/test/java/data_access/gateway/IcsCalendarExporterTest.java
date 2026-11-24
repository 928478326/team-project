package data_access.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import entity.Community;
import entity.Event;

class IcsCalendarExporterTest {

    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");
    private static final Instant NOW = Instant.parse("2026-11-24T17:00:00Z");

    private final IcsCalendarExporter exporter = new IcsCalendarExporter(Clock.fixed(NOW, ZoneOffset.UTC));
    private final List<Path> createdDirs = new ArrayList<>();

    private static Event event(String id, String title, String description, ZonedDateTime startsAt) {
        return new Event(id, Community.STUDY_ID, "u-james", title, description, 4, startsAt.toInstant());
    }

    private Path tempDir() throws IOException {
        final Path dir = Files.createTempDirectory("ics-test");
        createdDirs.add(dir);
        return dir;
    }

    @AfterEach
    void deleteTempFiles() throws IOException {
        for (Path dir : createdDirs) {
            final List<Path> paths = new ArrayList<>();
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.forEach(paths::add);
            }
            Collections.reverse(paths);
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }

    /** Undoes RFC 5545 line folding so assertions can look at whole logical lines. */
    private static List<String> unfoldedLines(String ics) {
        return List.of(ics.replace("\r\n ", "").split("\r\n"));
    }

    @Test
    void writesAValidCalendarWithOneVeventPerEvent() throws IOException {
        final Path file = tempDir().resolve("december.ics");
        final List<Event> events = List.of(
                event("e-1", "CSC207 Review", "Bahen 1180", ZonedDateTime.of(2026, 12, 3, 14, 0, 0, 0, TORONTO)),
                event("e-2", "Boardgames", "", ZonedDateTime.of(2026, 12, 6, 18, 0, 0, 0, TORONTO)));

        exporter.exportToIcs(events, TORONTO, file.toString());

        final String ics = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(ics.startsWith("BEGIN:VCALENDAR\r\nVERSION:2.0\r\n"));
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"));
        assertFalse(ics.replace("\r\n", "").contains("\n"), "every line must end with CRLF");
        final List<String> lines = unfoldedLines(ics);
        assertTrue(lines.contains("PRODID:-//CampusBuddy//CSC207 Team Project//EN"));
        assertTrue(lines.contains("X-WR-TIMEZONE:America/Toronto"));
        assertEquals(2, Collections.frequency(lines, "BEGIN:VEVENT"));
        assertEquals(2, Collections.frequency(lines, "END:VEVENT"));
        assertTrue(lines.contains("UID:e-1@campusbuddy.utoronto"));
        assertTrue(lines.contains("SUMMARY:CSC207 Review"));
        assertTrue(lines.contains("DESCRIPTION:Bahen 1180"));
        assertTrue(lines.contains("CATEGORIES:study"));
        assertEquals(1L, lines.stream().filter(l -> l.startsWith("DESCRIPTION:")).count(),
                "an event with no description gets no DESCRIPTION line");
    }

    @Test
    void torontoTimesAreWrittenInUtcWithAOneHourDuration() {
        final String ics = exporter.toIcs(
                List.of(event("e-1", "Review", "", ZonedDateTime.of(2026, 12, 3, 14, 0, 0, 0, TORONTO))),
                TORONTO);

        final List<String> lines = unfoldedLines(ics);
        // 14:00 in Toronto in December (EST, UTC-5) is 19:00 UTC.
        assertTrue(lines.contains("DTSTART:20261203T190000Z"));
        assertTrue(lines.contains("DTEND:20261203T200000Z"));
        assertTrue(lines.contains("DTSTAMP:20261124T170000Z"));
    }

    @Test
    void summerTimesUseDaylightSavingOffset() {
        final String ics = exporter.toIcs(
                List.of(event("e-1", "Picnic", "", ZonedDateTime.of(2026, 7, 1, 12, 0, 0, 0, TORONTO))),
                TORONTO);

        // 12:00 in Toronto in July (EDT, UTC-4) is 16:00 UTC.
        assertTrue(unfoldedLines(ics).contains("DTSTART:20260701T160000Z"));
    }

    @Test
    void specialCharactersAreEscaped() {
        final String ics = exporter.toIcs(List.of(event("e-1", "Hotpot, drinks; fun \\o/",
                "Line one\nLine two", ZonedDateTime.of(2026, 12, 3, 14, 0, 0, 0, TORONTO))), TORONTO);

        final List<String> lines = unfoldedLines(ics);
        assertTrue(lines.contains("SUMMARY:Hotpot\\, drinks\\; fun \\\\o/"));
        assertTrue(lines.contains("DESCRIPTION:Line one\\nLine two"));
    }

    @Test
    void longLinesAreFoldedWithoutBreakingChineseCharacters() {
        final String title = "多伦多大学期末复习小组：CSC207 Clean Architecture 和 SOLID 设计原则一起刷题，欢迎所有同学参加！";
        final String ics = exporter.toIcs(
                List.of(event("e-1", title, "", ZonedDateTime.of(2026, 12, 3, 14, 0, 0, 0, TORONTO))), TORONTO);

        for (String physicalLine : ics.split("\r\n")) {
            assertTrue(physicalLine.getBytes(StandardCharsets.UTF_8).length <= 75, physicalLine);
        }
        assertTrue(ics.split("\r\n").length > unfoldedLines(ics).size(), "the summary must have been folded");
        assertTrue(unfoldedLines(ics).contains("SUMMARY:" + title));
    }

    @Test
    void emptyMonthStillProducesAValidCalendar() {
        final List<String> lines = unfoldedLines(exporter.toIcs(List.of(), TORONTO));

        assertEquals("BEGIN:VCALENDAR", lines.get(0));
        assertEquals("END:VCALENDAR", lines.get(lines.size() - 1));
        assertFalse(lines.contains("BEGIN:VEVENT"));
    }

    @Test
    void missingFoldersAreCreated() throws IOException {
        final Path file = tempDir().resolve("exports").resolve("2026").resolve("dec.ics");

        exporter.exportToIcs(List.of(), TORONTO, file.toString());

        assertTrue(Files.exists(file));
    }

    @Test
    void blankPathIsAnIoError() {
        boolean threw = false;
        try {
            exporter.exportToIcs(List.of(), TORONTO, "   ");
        }
        catch (IOException ex) {
            threw = true;
        }
        assertTrue(threw);
    }
}
