package use_case.view_calendar;

/**
 * Input boundary for viewing a student's personal monthly calendar and exporting to {@code .ics}.
 */
public interface ViewCalendarInputBoundary {

    /**
     * Loads the joined events and public holidays for the requested year/month,
     * optionally exporting that month's events to an iCalendar file if {@code exportFilePath}
     * is provided.
     *
     * @param inputData calendar query and optional export path
     */
    void execute(ViewCalendarInputData inputData);
}
