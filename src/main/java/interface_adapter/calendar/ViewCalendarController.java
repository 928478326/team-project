package interface_adapter.calendar;

import java.util.Objects;
import use_case.view_calendar.ViewCalendarInputBoundary;
import use_case.view_calendar.ViewCalendarInputData;

/**
 * Controller for loading a monthly calendar and optionally exporting {@code .ics}.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class ViewCalendarController {

    private final ViewCalendarInputBoundary interactor;

    public ViewCalendarController(ViewCalendarInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    /**
     * Loads the specified month's schedule and public holidays.
     *
     * @param userId active user ID
     * @param year calendar year
     * @param month calendar month (1..12)
     */
    public void loadMonth(String userId, int year, int month) {
        interactor.execute(new ViewCalendarInputData(userId, year, month, ""));
    }

    /**
     * Loads and exports the specified month's joined events to {@code exportFilePath}.
     *
     * @param userId active user ID
     * @param year calendar year
     * @param month calendar month (1..12)
     * @param exportFilePath target {@code .ics} path
     */
    public void exportMonth(String userId, int year, int month, String exportFilePath) {
        interactor.execute(new ViewCalendarInputData(userId, year, month, exportFilePath));
    }
}
