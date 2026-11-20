package use_case.browse_events;

/**
 * Input boundary for browsing community events.
 */
public interface BrowseEventsInputBoundary {

    /**
     * Loads events for a community filter (or {@code "ALL"}).
     *
     * @param inputData query parameters including current user ID and community filter
     */
    void execute(BrowseEventsInputData inputData);
}
