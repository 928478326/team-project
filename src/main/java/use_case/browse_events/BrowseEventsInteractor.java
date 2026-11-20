package use_case.browse_events;

import java.time.Clock;
import java.util.Objects;

/**
 * Interactor for browsing events within a community (or across all communities).
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class BrowseEventsInteractor implements BrowseEventsInputBoundary {

    private final BrowseEventsDataAccessInterface dataAccess;
    private final BrowseEventsOutputBoundary outputBoundary;
    private final Clock clock;

    public BrowseEventsInteractor(
            BrowseEventsDataAccessInterface dataAccess,
            BrowseEventsOutputBoundary outputBoundary,
            Clock clock) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void execute(BrowseEventsInputData inputData) {
        // TODO [Track B]:
        // 1. Validate communityId (either ALL_COMMUNITIES or dataAccess.communityExists(id)).
        // 2. Load events via dataAccess.findEventsByCommunity(...), sort by startsAt ASC then eventId ASC.
        // 3. Map each Event -> BrowseEventsOutputData.EventItem using countParticipants,
        //    hasUserJoined, findUserById, and event.isOpenAt(clock.instant()).
        // 4. Present via outputBoundary.prepareSuccessView(...).
        outputBoundary.prepareFailView("TODO [Track B]: Implement BrowseEventsInteractor.execute");
    }
}
