package use_case.view_calendar;

/**
 * Output boundary for the View Calendar use case.
 */
public interface ViewCalendarOutputBoundary {

    /**
     * Presents the monthly schedule, public holidays, and any export confirmation.
     *
     * @param outputData monthly calendar DTOs
     */
    void prepareSuccessView(ViewCalendarOutputData outputData);

    /**
     * Presents an error when loading or exporting the calendar.
     *
     * @param userId the student whose calendar was requested ({@code ""} if nobody was signed in),
     *     so a presenter can ignore failures that arrive after that student signed out
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String userId, String errorMessage);
}
