package use_case.view_calendar;

import java.time.ZoneId;
import java.util.Objects;

/**
 * Interactor implementing the monthly View Calendar (+ Holiday lookup + {@code .ics} export)
 * use case.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class ViewCalendarInteractor implements ViewCalendarInputBoundary {

    /** Country code used for UofT / Ontario public holiday lookups. */
    public static final String DEFAULT_COUNTRY_CODE = "CA";

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
        // TODO [Track B]:
        // 1. Validate userId non-blank and valid YearMonth (month 1..12).
        // 2. Compute [firstDay 00:00 in zoneId, nextMonthFirstDay 00:00 in zoneId) as Instants.
        // 3. Query dataAccess.findJoinedEventsBetween(userId, start, end).
        // 4. Query holidayGateway.fetchPublicHolidays(year, DEFAULT_COUNTRY_CODE), filtering
        //    to the target month (silently degrade to empty list on runtime failure).
        // 5. If inputData.isExportRequested(), invoke exportGateway.exportToIcs(...) and set
        //    statusMessage (or prepareFailView on IOException).
        // 6. Present via outputBoundary.prepareSuccessView(...).
        outputBoundary.prepareFailView("TODO [Track B]: Implement ViewCalendarInteractor.execute");
    }
}
