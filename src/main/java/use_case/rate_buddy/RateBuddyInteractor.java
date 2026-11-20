package use_case.rate_buddy;

import java.time.Clock;
import java.util.Objects;

/**
 * Interactor implementing the Rate Buddy use case.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class RateBuddyInteractor implements RateBuddyInputBoundary {

    private final RateBuddyDataAccessInterface dataAccess;
    private final RateBuddyOutputBoundary outputBoundary;
    private final Clock clock;

    public RateBuddyInteractor(
            RateBuddyDataAccessInterface dataAccess,
            RateBuddyOutputBoundary outputBoundary,
            Clock clock) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void execute(RateBuddyInputData inputData) {
        // TODO [Track C]:
        // 1. Validate raterId, rateeId, eventId non-blank and !raterId.equals(rateeId).
        // 2. Verify dataAccess.haveUsersSharedEvent(raterId, rateeId, eventId).
        // 3. Construct BuddyRating(..., clock.instant()) (catching IllegalArgumentException
        //    for score range / comment length -> prepareFailView).
        // 4. Call dataAccess.upsertRating(rating) and outputBoundary.prepareSuccessView(...).
        outputBoundary.prepareFailView("TODO [Track C]: Implement RateBuddyInteractor.execute");
    }
}
