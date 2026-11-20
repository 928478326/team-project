package use_case.view_calendar;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable output data for a student's monthly calendar view.
 */
public final class ViewCalendarOutputData {

    private final int year;
    private final int month;
    private final String zoneId;
    private final List<CalendarEventItem> events;
    private final List<HolidayItem> holidays;
    private final String statusMessage;

    public ViewCalendarOutputData(
            int year,
            int month,
            String zoneId,
            List<CalendarEventItem> events,
            List<HolidayItem> holidays,
            String statusMessage) {
        this.year = year;
        this.month = month;
        this.zoneId = zoneId;
        this.events = events == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(events));
        this.holidays = holidays == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(holidays));
        this.statusMessage = statusMessage == null ? "" : statusMessage;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public String getZoneId() {
        return zoneId;
    }

    public List<CalendarEventItem> getEvents() {
        return events;
    }

    public List<HolidayItem> getHolidays() {
        return holidays;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Immutable DTO describing one joined event on the calendar.
     */
    public static final class CalendarEventItem {

        private final String eventId;
        private final String communityName;
        private final String title;
        private final Instant startsAt;

        public CalendarEventItem(
                String eventId, String communityName, String title, Instant startsAt) {
            this.eventId = eventId;
            this.communityName = communityName;
            this.title = title;
            this.startsAt = startsAt;
        }

        public String getEventId() {
            return eventId;
        }

        public String getCommunityName() {
            return communityName;
        }

        public String getTitle() {
            return title;
        }

        public Instant getStartsAt() {
            return startsAt;
        }
    }

    /**
     * Immutable DTO describing a public holiday falling in the viewed month.
     */
    public static final class HolidayItem {

        private final LocalDate date;
        private final String name;

        public HolidayItem(LocalDate date, String name) {
            this.date = date;
            this.name = name;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getName() {
            return name;
        }
    }
}
