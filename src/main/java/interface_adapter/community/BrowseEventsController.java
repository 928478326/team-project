package interface_adapter.community;

import java.util.Objects;
import use_case.browse_events.BrowseEventsInputBoundary;
import use_case.browse_events.BrowseEventsInputData;

/**
 * Controller for browsing events by community.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class BrowseEventsController {

    private final BrowseEventsInputBoundary interactor;

    public BrowseEventsController(BrowseEventsInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    /**
     * Triggers loading events for the selected community.
     *
     * @param currentUserId active user ID read by the View
     * @param communityId community ID or {@code "ALL"}
     */
    public void execute(String currentUserId, String communityId) {
        interactor.execute(new BrowseEventsInputData(currentUserId, communityId));
    }
}
