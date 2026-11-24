package use_case.view_calendar;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import entity.Community;
import entity.Event;
import use_case.view_calendar.PublicHolidayGateway.PublicHoliday;

/**
 * Interactor implementing the monthly View Calendar (+ Holiday lookup + {@code .ics} export)
 * use case.
 *
 * <p>The month runs from the 1st at 00:00 up to (not including) the 1st of the next month at
 * 00:00, both in the configured zone (Toronto). If the holiday API fails, the calendar still
 * loads, just without holidays. If an export is requested and fails, the whole request fails so
 * the student is not told a file was written when it was not.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class ViewCalendarInteractor implements ViewCalendarInputBoundary {

    /** Country code used for UofT / Ontario public holiday lookups. */
    public static final String DEFAULT_COUNTRY_CODE = "CA";

    private static final Comparator<Event> EVENT_ORDER =
            Comparator.comparing(Event::getStartsAt).thenComparing(Event::getEventId);

    private final ViewCalendarDataAccessInterface dataAccess;
    private final PublicHolidayGateway holidayGateway;
    private final CalendarExportGateway exportGateway;
    private final ViewCalendarOutputBoundary outputBoundary;
    private final ZoneId zoneId;

    public ViewCalendarInteractor(
            ViewCalendarDataAccessInterface dataAccess,
            PublicHolidayGateway holidayGateway,
            CalendarExportGateway exportGateway,
            ViewCalendarOutputBoundary outputBoundary,
            ZoneId zoneId) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.holidayGateway =
                Objects.requireNonNull(holidayGateway, "holidayGateway must not be null");
        this.exportGateway =
                Objects.requireNonNull(exportGateway, "exportGateway must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId must not be null");
    }

    @Override
    public void execute(ViewCalendarInputData inputData) {
        final Optional<YearMonth> yearMonth = toYearMonth(inputData.getYear(), inputData.getMonth());
        if (inputData.getUserId().isEmpty()) {
            outputBoundary.prepareFailView("Sign in to view your calendar.");
        }
        else if (yearMonth.isEmpty()) {
            outputBoundary.prepareFailView("Choose a valid month (1-12).");
        }
        else {
            loadMonth(inputData, yearMonth.get());
        }
    }

    private void loadMonth(ViewCalendarInputData inputData, YearMonth yearMonth) {
        final Instant start = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant();
        final Instant end = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant();
        List<Event> events;
        try {
            events = new ArrayList<>(dataAccess.findJoinedEventsBetween(inputData.getUserId(), start, end));
            events.sort(EVENT_ORDER);
        }
        catch (IllegalStateException ex) {
            events = null;
        }

        if (events == null) {
            outputBoundary.prepareFailView("Could not load your calendar. Please try again.");
        }
        else if (inputData.isExportRequested()) {
            exportThenPresent(inputData.getExportFilePath(), yearMonth, events);
        }
        else {
            present(yearMonth, events, "");
        }
    }

    private void exportThenPresent(String exportFilePath, YearMonth yearMonth, List<Event> events) {
        String failure = "";
        try {
            exportGateway.exportToIcs(events, zoneId, exportFilePath);
        }
        catch (IOException ex) {
            failure = "Could not export the calendar: " + ex.getMessage();
        }
        if (failure.isEmpty()) {
            present(yearMonth, events, "Exported " + events.size() + " event(s) to " + exportFilePath);
        }
        else {
            outputBoundary.prepareFailView(failure);
        }
    }

    private void present(YearMonth yearMonth, List<Event> events, String statusMessage) {
        final Map<String, String> communityNames = communityNamesById();
        final List<ViewCalendarOutputData.CalendarEventItem> eventItems = new ArrayList<>();
        for (Event event : events) {
            eventItems.add(new ViewCalendarOutputData.CalendarEventItem(
                    event.getEventId(),
                    communityNames.getOrDefault(event.getCommunityId(), event.getCommunityId()),
                    event.getTitle(),
                    event.getStartsAt()));
        }
        outputBoundary.prepareSuccessView(new ViewCalendarOutputData(
                yearMonth.getYear(),
                yearMonth.getMonthValue(),
                zoneId.getId(),
                eventItems,
                holidaysIn(yearMonth),
                statusMessage));
    }

    /**
     * Fetches the year's holidays and keeps only those in {@code yearMonth}, sorted by date.
     * Any failure from the external API means "no holidays", never a failed calendar.
     */
    private List<ViewCalendarOutputData.HolidayItem> holidaysIn(YearMonth yearMonth) {
        List<PublicHoliday> fetched;
        try {
            fetched = holidayGateway.fetchPublicHolidays(yearMonth.getYear(), DEFAULT_COUNTRY_CODE);
        }
        catch (IllegalStateException ex) {
            fetched = null;
        }

        final List<ViewCalendarOutputData.HolidayItem> holidays = new ArrayList<>();
        if (fetched != null) {
            for (PublicHoliday holiday : fetched) {
                if (holiday != null && holiday.getDate() != null
                        && YearMonth.from(holiday.getDate()).equals(yearMonth)) {
                    holidays.add(new ViewCalendarOutputData.HolidayItem(holiday.getDate(), holidayName(holiday)));
                }
            }
        }
        holidays.sort(Comparator.comparing(ViewCalendarOutputData.HolidayItem::getDate));
        return holidays;
    }

    /**
     * Prefers the local name (e.g. "Boxing Day") over Nager.Date's international name
     * (e.g. "St. Stephen's Day"), since that is what students in Toronto call it.
     */
    private static String holidayName(PublicHoliday holiday) {
        final String name;
        if (holiday.getLocalName() != null && !holiday.getLocalName().isBlank()) {
            name = holiday.getLocalName();
        }
        else if (holiday.getName() != null) {
            name = holiday.getName();
        }
        else {
            name = "";
        }
        return name;
    }

    private Map<String, String> communityNamesById() {
        final Map<String, String> names = new HashMap<>();
        for (Community community : dataAccess.findAllCommunities()) {
            names.put(community.getCommunityId(), community.getName());
        }
        return names;
    }

    private static Optional<YearMonth> toYearMonth(int year, int month) {
        Optional<YearMonth> result;
        try {
            result = Optional.of(YearMonth.of(year, month));
        }
        catch (DateTimeException ex) {
            result = Optional.empty();
        }
        return result;
    }
}
