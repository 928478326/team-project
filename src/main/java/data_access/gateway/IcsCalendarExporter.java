package data_access.gateway;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import entity.Event;
import use_case.view_calendar.CalendarExportGateway;

/**
 * Exports CampusBuddy events to RFC 5545 iCalendar ({@code .ics}) files compatible with
 * Google Calendar and Apple Calendar.
 *
 * <p>Times are written in UTC ({@code ...Z}), which every calendar app understands without a
 * separate time-zone definition; the app then shows them in the student's own zone. Events have
 * no end time in CampusBuddy, so each one is exported as {@link #DEFAULT_EVENT_DURATION} long.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class IcsCalendarExporter implements CalendarExportGateway {

    /** Length given to every exported event, since CampusBuddy events only have a start time. */
    public static final Duration DEFAULT_EVENT_DURATION = Duration.ofHours(1);

    /** RFC 5545 line terminator. */
    private static final String CRLF = "\r\n";
    /** RFC 5545 escape for a line break inside a text value: a backslash followed by {@code n}. */
    private static final String ESCAPED_NEWLINE = "\\n";
    /** RFC 5545 section 3.1: content lines should not exceed 75 octets. */
    private static final int MAX_LINE_OCTETS = 75;
    private static final DateTimeFormatter UTC_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final String UID_DOMAIN = "@campusbuddy.utoronto";

    private final Clock clock;

    /** Creates an exporter stamping files with the current system time. */
    public IcsCalendarExporter() {
        this(Clock.systemUTC());
    }

    /**
     * Creates an exporter with an injected clock (used by tests for a fixed {@code DTSTAMP}).
     *
     * @param clock source of the export timestamp
     */
    public IcsCalendarExporter(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void exportToIcs(List<Event> events, ZoneId zoneId, String targetFilePath)
            throws IOException {
        if (targetFilePath == null || targetFilePath.isBlank()) {
            throw new IOException("No export file was chosen.");
        }
        final Path target;
        try {
            target = Paths.get(targetFilePath.trim());
        }
        catch (InvalidPathException ex) {
            throw new IOException("Invalid export path: " + targetFilePath, ex);
        }
        final Path parent = target.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(target, toIcs(events, zoneId).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Builds the full iCalendar document.
     *
     * @param events events to include (null is treated as none)
     * @param zoneId the student's zone, advertised to calendar apps as the display zone
     * @return iCalendar text with CRLF line endings
     */
    String toIcs(List<Event> events, ZoneId zoneId) {
        final StringBuilder ics = new StringBuilder();
        line(ics, "BEGIN:VCALENDAR");
        line(ics, "VERSION:2.0");
        line(ics, "PRODID:-//CampusBuddy//CSC207 Team Project//EN");
        line(ics, "CALSCALE:GREGORIAN");
        line(ics, "METHOD:PUBLISH");
        line(ics, "X-WR-CALNAME:CampusBuddy");
        if (zoneId != null) {
            line(ics, "X-WR-TIMEZONE:" + zoneId.getId());
        }
        final String stamp = UTC_FORMAT.format(clock.instant());
        if (events != null) {
            for (Event event : events) {
                appendEvent(ics, event, stamp);
            }
        }
        line(ics, "END:VCALENDAR");
        return ics.toString();
    }

    private static void appendEvent(StringBuilder ics, Event event, String stamp) {
        final Instant start = event.getStartsAt();
        line(ics, "BEGIN:VEVENT");
        line(ics, "UID:" + escapeText(event.getEventId()) + UID_DOMAIN);
        line(ics, "DTSTAMP:" + stamp);
        line(ics, "DTSTART:" + UTC_FORMAT.format(start));
        line(ics, "DTEND:" + UTC_FORMAT.format(start.plus(DEFAULT_EVENT_DURATION)));
        line(ics, "SUMMARY:" + escapeText(event.getTitle()));
        if (!event.getDescription().isEmpty()) {
            line(ics, "DESCRIPTION:" + escapeText(event.getDescription()));
        }
        line(ics, "CATEGORIES:" + escapeText(event.getCommunityId()));
        line(ics, "END:VEVENT");
    }

    /**
     * Escapes TEXT values per RFC 5545 section 3.3.11.
     *
     * @param text raw text
     * @return text with backslashes, semicolons, commas, and newlines escaped
     */
    static String escapeText(String text) {
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", ESCAPED_NEWLINE)
                .replace("\n", ESCAPED_NEWLINE)
                .replace("\r", ESCAPED_NEWLINE);
    }

    /**
     * Appends one content line, folding it so no physical line exceeds 75 UTF-8 bytes.
     * Continuation lines start with a single space (RFC 5545 section 3.1). Folding never splits
     * a multi-byte character such as a Chinese character or emoji.
     *
     * @param ics calendar text being built
     * @param content one unfolded content line, without its line ending
     */
    private static void line(StringBuilder ics, String content) {
        int lineOctets = 0;
        int offset = 0;
        while (offset < content.length()) {
            final int codePoint = content.codePointAt(offset);
            final int charCount = Character.charCount(codePoint);
            final int octets = new String(Character.toChars(codePoint)).getBytes(StandardCharsets.UTF_8).length;
            if (lineOctets + octets > MAX_LINE_OCTETS) {
                ics.append(CRLF).append(' ');
                // The leading space counts toward the continuation line's length.
                lineOctets = 1;
            }
            ics.append(content, offset, offset + charCount);
            lineOctets += octets;
            offset += charCount;
        }
        ics.append(CRLF);
    }
}
