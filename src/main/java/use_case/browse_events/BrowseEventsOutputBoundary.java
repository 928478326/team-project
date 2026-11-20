package use_case.browse_events;

/**
 * Output boundary for browsing community events.
 */
public interface BrowseEventsOutputBoundary {

    /**
     * Presents the ordered list of events for the selected community.
     *
     * @param outputData event list and available community options
     */
    void prepareSuccessView(BrowseEventsOutputData outputData);

    /**
     * Presents a query or validation error.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
