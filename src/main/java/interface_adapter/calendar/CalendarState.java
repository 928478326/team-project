package interface_adapter.calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UI state for the personal monthly Calendar view.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CalendarState {

    private int year = 2026;
    private int month = 12;
    private String zoneLabel = "America/Toronto";
    private List<CalendarEventRow> events = Collections.emptyList();
    private List<String> holidayLabels = Collections.emptyList();
    private String statusMessage = "";
    private String errorMessage = "";

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getZoneLabel() {
        return zoneLabel;
    }

    public void setZoneLabel(String zoneLabel) {
        this.zoneLabel = zoneLabel == null ? "" : zoneLabel;
    }

    public List<CalendarEventRow> getEvents() {
        return events;
    }

    public void setEvents(List<CalendarEventRow> events) {
        this.events = events == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(events));
    }

    public List<String> getHolidayLabels() {
        return holidayLabels;
    }

    public void setHolidayLabels(List<String> holidayLabels) {
        this.holidayLabels = holidayLabels == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(holidayLabels));
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage == null ? "" : statusMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? "" : errorMessage;
    }

    /**
     * Formatted calendar event row for Swing rendering.
     */
    public static final class CalendarEventRow {

        private final String eventId;
        private final String formattedDateTime;
        private final String communityName;
        private final String title;

        public CalendarEventRow(
                String eventId, String formattedDateTime, String communityName, String title) {
            this.eventId = eventId;
            this.formattedDateTime = formattedDateTime;
            this.communityName = communityName;
            this.title = title;
        }

        public String getEventId() {
            return eventId;
        }

        public String getFormattedDateTime() {
            return formattedDateTime;
        }

        public String getCommunityName() {
            return communityName;
        }

        public String getTitle() {
            return title;
        }
    }
}
