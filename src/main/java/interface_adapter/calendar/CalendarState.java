package interface_adapter.calendar;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * UI state for the personal monthly Calendar view.
 *
 * <p>Rows carry their day of the month so the view can place them in a month grid without
 * re-parsing formatted text.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CalendarState {

    /** Zone shown before the first load; the presenter replaces it with the app's zone. */
    public static final String DEFAULT_ZONE = "America/Toronto";

    private int year;
    private int month;
    private String zoneLabel = DEFAULT_ZONE;
    private List<CalendarEventRow> events = Collections.emptyList();
    private List<HolidayRow> holidays = Collections.emptyList();
    private String statusMessage = "";
    private String errorMessage = "";
    private String ownerUserId = "";

    /** Starts on the current month in Toronto. */
    public CalendarState() {
        final YearMonth now = YearMonth.now(ZoneId.of(DEFAULT_ZONE));
        this.year = now.getYear();
        this.month = now.getMonthValue();
    }

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
        this.zoneLabel = Objects.requireNonNullElse(zoneLabel, "");
    }

    public List<CalendarEventRow> getEvents() {
        return events;
    }

    public void setEvents(List<CalendarEventRow> events) {
        this.events = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNullElse(events, Collections.emptyList())));
    }

    public List<HolidayRow> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<HolidayRow> holidays) {
        this.holidays = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNullElse(holidays, Collections.emptyList())));
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = Objects.requireNonNullElse(statusMessage, "");
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = Objects.requireNonNullElse(errorMessage, "");
    }

    /**
     * Returns the student whose events and messages this state holds.
     *
     * @return owner user ID, or {@code ""} before any calendar has loaded
     */
    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = Objects.requireNonNullElse(ownerUserId, "");
    }

    /**
     * Formatted calendar event row for Swing rendering.
     */
    public static final class CalendarEventRow {

        private final String eventId;
        private final String formattedDateTime;
        private final String communityName;
        private final String title;
        private final int dayOfMonth;
        private final String timeText;

        /**
         * Creates a row.
         *
         * @param eventId event ID
         * @param formattedDateTime full local date and time, e.g. {@code Thu Dec 3, 14:00}
         * @param communityName community display name
         * @param title event title
         * @param dayOfMonth local day of the month (1..31) the event starts on
         * @param timeText local start time, e.g. {@code 14:00}
         */
        public CalendarEventRow(
                String eventId,
                String formattedDateTime,
                String communityName,
                String title,
                int dayOfMonth,
                String timeText) {
            this.eventId = eventId;
            this.formattedDateTime = formattedDateTime;
            this.communityName = communityName;
            this.title = title;
            this.dayOfMonth = dayOfMonth;
            this.timeText = timeText;
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

        public int getDayOfMonth() {
            return dayOfMonth;
        }

        public String getTimeText() {
            return timeText;
        }
    }

    /**
     * Public holiday on a given day of the displayed month.
     */
    public static final class HolidayRow {

        private final int dayOfMonth;
        private final String name;

        public HolidayRow(int dayOfMonth, String name) {
            this.dayOfMonth = dayOfMonth;
            this.name = name;
        }

        public int getDayOfMonth() {
            return dayOfMonth;
        }

        public String getName() {
            return name;
        }
    }
}
