package use_case.view_calendar;

import entity.Event;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

/**
 * External gateway abstraction for exporting events to standard iCalendar ({@code .ics}) format.
 * Decouples calendar use-case logic from filesystem I/O (DIP).
 */
public interface CalendarExportGateway {

    /**
     * Writes the supplied events to a valid RFC 5545 {@code .ics} file at {@code targetFilePath}.
     *
     * @param events events to export
     * @param zoneId display timezone
     * @param targetFilePath destination path on disk
     * @throws IOException if writing fails
     */
    void exportToIcs(List<Event> events, ZoneId zoneId, String targetFilePath) throws IOException;
}
