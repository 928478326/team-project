package use_case.view_calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import data_access.in_memory.InMemoryCampusBuddyStore;
import data_access.in_memory.InMemoryEventDataAccess;
import entity.Community;
import entity.Event;
import entity.Participation;
import use_case.view_calendar.PublicHolidayGateway.PublicHoliday;
import use_case.view_calendar.ViewCalendarOutputData.CalendarEventItem;
import use_case.view_calendar.ViewCalendarOutputData.HolidayItem;

class ViewCalendarInteractorTest {

    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");
    private static final String USER = "u-gracie";

    private final InMemoryCampusBuddyStore store = new InMemoryCampusBuddyStore();
    private final FakeHolidayGateway holidays = new FakeHolidayGateway();
    private final FakeExporter exporter = new FakeExporter();
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final ViewCalendarInteractor interactor = new ViewCalendarInteractor(
            new InMemoryEventDataAccess(store), holidays, exporter, presenter, TORONTO);

    ViewCalendarInteractorTest() {
        joined("e-dec-3", Community.STUDY_ID, ZonedDateTime.of(2026, 12, 3, 14, 0, 0, 0, TORONTO).toInstant());
        // First instant of December in Toronto: belongs to December.
        joined("e-dec-1", Community.GAMING_ID, ZonedDateTime.of(2026, 12, 1, 0, 0, 0, 0, TORONTO).toInstant());
        // Already December 1 in UTC, but still November 30 22:00 in Toronto: belongs to November.
        joined("e-nov-30", Community.FOOD_ID, Instant.parse("2026-12-01T03:00:00Z"));
        // First instant of January in Toronto: excluded from December.
        joined("e-jan-1", Community.CHILL_ID, ZonedDateTime.of(2027, 1, 1, 0, 0, 0, 0, TORONTO).toInstant());
        // In December, but this student never joined it.
        store.putEvent(new Event("e-not-mine", Community.STUDY_ID, "u-james", "Other", "", 4,
                ZonedDateTime.of(2026, 12, 10, 10, 0, 0, 0, TORONTO).toInstant()));

        holidays.result = List.of(
                new PublicHoliday(LocalDate.of(2026, 12, 26), "Boxing Day", "St. Stephen's Day"),
                new PublicHoliday(LocalDate.of(2026, 11, 11), "Remembrance Day", "Remembrance Day"),
                new PublicHoliday(LocalDate.of(2026, 12, 25), "Christmas Day", "Christmas Day"),
                new PublicHoliday(LocalDate.of(2026, 1, 1), "New Year's Day", "New Year's Day"));
    }

    private void joined(String eventId, String communityId, Instant startsAt) {
        store.putEvent(new Event(eventId, communityId, "u-james", "Title " + eventId, "", 4, startsAt));
        store.putParticipation(new Participation(USER, eventId, Instant.EPOCH));
    }

    private static List<String> eventIds(List<CalendarEventItem> items) {
        final List<String> ids = new ArrayList<>();
        for (CalendarEventItem item : items) {
            ids.add(item.getEventId());
        }
        return ids;
    }

    private static List<LocalDate> holidayDates(List<HolidayItem> items) {
        final List<LocalDate> dates = new ArrayList<>();
        for (HolidayItem item : items) {
            dates.add(item.getDate());
        }
        return dates;
    }

    @Test
    void monthShowsOnlyJoinedEventsInsideTheTorontoMonth() {
        interactor.execute(new ViewCalendarInputData(USER, 2026, 12, ""));

        assertNull(presenter.error);
        assertEquals(USER, presenter.success.getUserId());
        assertEquals(2026, presenter.success.getYear());
        assertEquals(12, presenter.success.getMonth());
        assertEquals("America/Toronto", presenter.success.getZoneId());
        assertEquals(List.of("e-dec-1", "e-dec-3"), eventIds(presenter.success.getEvents()));
        assertEquals("Gaming", presenter.success.getEvents().get(0).getCommunityName());
        assertEquals("", presenter.success.getStatusMessage());
    }

    @Test
    void lateNightEventCountsInTheTorontoMonthNotTheUtcMonth() {
        interactor.execute(new ViewCalendarInputData(USER, 2026, 11, ""));

        assertEquals(List.of("e-nov-30"), eventIds(presenter.success.getEvents()));
    }

    @Test
    void onlyThisMonthsHolidaysAreShownInDateOrder() {
        interactor.execute(new ViewCalendarInputData(USER, 2026, 12, ""));

        assertEquals(2026, holidays.requestedYear);
        assertEquals(ViewCalendarInteractor.DEFAULT_COUNTRY_CODE, holidays.requestedCountry);
        assertEquals(List.of(LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26)),
                holidayDates(presenter.success.getHolidays()));
        assertEquals("Christmas Day", presenter.success.getHolidays().get(0).getName());
        assertEquals("Boxing Day", presenter.success.getHolidays().get(1).getName());
    }

    @Test
    void holidayApiFailureStillShowsTheCalendar() {
        holidays.failure = new IllegalStateException("network down");

        interactor.execute(new ViewCalendarInputData(USER, 2026, 12, ""));

        assertNull(presenter.error);
        assertTrue(presenter.success.getHolidays().isEmpty());
        assertEquals(2, presenter.success.getEvents().size());
    }

    @Test
    void nullHolidayListIsTreatedAsNoHolidays() {
        holidays.result = null;

        interactor.execute(new ViewCalendarInputData(USER, 2026, 12, ""));

        assertNull(presenter.error);
        assertTrue(presenter.success.getHolidays().isEmpty());
    }

    @Test
    void viewingWithoutAnExportPathDoesNotWriteFile() {
        interactor.execute(new ViewCalendarInputData(USER, 2026, 12, "  "));

        assertNull(exporter.path);
    }

    @Test
    void exportWritesThatMonthsEventsAndReportsThePath() {
        interactor.execute(new ViewCalendarInputData(USER, 2026, 12, "out/december.ics"));

        assertNull(presenter.error);
        assertEquals("out/december.ics", exporter.path);
        assertEquals(TORONTO, exporter.zoneId);
        assertEquals(List.of("e-dec-1", "e-dec-3"), exporter.eventIds);
        assertTrue(presenter.success.getStatusMessage().contains("out/december.ics"));
    }

    @Test
    void exportFailureIsReportedToTheUser() {
        exporter.failure = new IOException("disk full");

        interactor.execute(new ViewCalendarInputData(USER, 2026, 12, "out/december.ics"));

        assertNull(presenter.success);
        assertTrue(presenter.error.contains("disk full"));
        assertEquals(USER, presenter.errorUserId);
    }

    @Test
    void storageFailureWhileReadingCommunityNamesIsReportedToTheUser() {
        final InMemoryEventDataAccess events = new InMemoryEventDataAccess(store);
        final ViewCalendarDataAccessInterface failingDao = new ViewCalendarDataAccessInterface() {
            @Override
            public List<Event> findJoinedEventsBetween(String userId, Instant start, Instant end) {
                return events.findJoinedEventsBetween(userId, start, end);
            }

            @Override
            public List<Community> findAllCommunities() {
                throw new IllegalStateException("database is locked");
            }
        };
        final ViewCalendarInteractor failing =
                new ViewCalendarInteractor(failingDao, holidays, exporter, presenter, TORONTO);

        failing.execute(new ViewCalendarInputData(USER, 2026, 12, "out/december.ics"));

        assertNull(presenter.success);
        assertNotNull(presenter.error);
        assertNull(exporter.path);
    }

    @ParameterizedTest
    @CsvSource({"u-gracie, 0", "u-gracie, 13", "'', 12"})
    void invalidRequestsAreRejected(String userId, int month) {
        interactor.execute(new ViewCalendarInputData(userId, 2026, month, ""));

        assertNull(presenter.success);
        assertNotNull(presenter.error);
        assertNull(holidays.requestedCountry);
    }

    /** Returns a fixed holiday list (or throws) and remembers what was asked for. */
    private static final class FakeHolidayGateway implements PublicHolidayGateway {
        private List<PublicHoliday> result = List.of();
        private RuntimeException failure;
        private int requestedYear;
        private String requestedCountry;

        @Override
        public List<PublicHoliday> fetchPublicHolidays(int year, String countryCode) {
            requestedYear = year;
            requestedCountry = countryCode;
            if (failure != null) {
                throw failure;
            }
            return result;
        }
    }

    /** Remembers what it was asked to export instead of writing a file. */
    private static final class FakeExporter implements CalendarExportGateway {
        private IOException failure;
        private String path;
        private ZoneId zoneId;
        private List<String> eventIds;

        @Override
        public void exportToIcs(List<Event> events, ZoneId zone, String targetFilePath) throws IOException {
            if (failure != null) {
                throw failure;
            }
            path = targetFilePath;
            zoneId = zone;
            eventIds = new ArrayList<>();
            for (Event event : events) {
                eventIds.add(event.getEventId());
            }
        }
    }

    /** Captures whatever the interactor presents. */
    private static final class RecordingPresenter implements ViewCalendarOutputBoundary {
        private ViewCalendarOutputData success;
        private String error;
        private String errorUserId;

        @Override
        public void prepareSuccessView(ViewCalendarOutputData outputData) {
            success = outputData;
        }

        @Override
        public void prepareFailView(String userId, String errorMessage) {
            errorUserId = userId;
            error = errorMessage;
        }
    }
}
