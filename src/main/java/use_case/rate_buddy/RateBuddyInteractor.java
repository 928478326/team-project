package use_case.rate_buddy;

import java.time.Clock;
import java.util.Objects;

import entity.BuddyRating;

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
        final String error = save(inputData);
        if (error.isEmpty()) {
            outputBoundary.prepareSuccessView(new RateBuddyOutputData(inputData.getRateeId(),
                    inputData.getEventId(), inputData.getScore(), inputData.isWouldMeetAgain(), "Rating saved."));
        }
        else {
            outputBoundary.prepareFailView(error);
        }
    }

    private String save(RateBuddyInputData input) {
        String error = "";
        try {
            final BuddyRating rating = new BuddyRating(input.getRaterId(), input.getRateeId(), input.getEventId(),
                    input.getScore(), input.isWouldMeetAgain(), input.getComment(), clock.instant());
            if (!dataAccess.saveIfEligible(rating)) {
                error = "You can only rate someone from a shared event that has started.";
            }
        }
        catch (IllegalArgumentException invalidRating) {
            error = invalidRating.getMessage();
        }
        catch (IllegalStateException unavailable) {
            error = "Could not save your rating. Please try again.";
        }
        return error;
    }
}
