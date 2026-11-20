package data_access.gateway;

import entity.Event;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import use_case.view_calendar.CalendarExportGateway;

/**
 * Exports CampusBuddy events to RFC 5545 iCalendar ({@code .ics}) files compatible with
 * Google Calendar and Apple Calendar.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class IcsCalendarExporter implements CalendarExportGateway {

    @Override
    public void exportToIcs(List<Event> events, ZoneId zoneId, String targetFilePath)
            throws IOException {
        // TODO [Track B]: Format VCALENDAR / VEVENT blocks (UID, DTSTAMP, DTSTART, DTEND, SUMMARY,
        // DESCRIPTION) using UTF-8 and write to targetFilePath via java.nio.file.Files.
        throw new IOException("TODO [Track B]: Implement IcsCalendarExporter.exportToIcs");
    }
}
