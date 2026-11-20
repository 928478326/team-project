package interface_adapter.buddy_rating;

import java.util.Objects;
import use_case.rate_buddy.RateBuddyInputBoundary;
import use_case.rate_buddy.RateBuddyInputData;

/**
 * Controller for submitting or updating a buddy rating.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class RateBuddyController {

    private final RateBuddyInputBoundary interactor;

    public RateBuddyController(RateBuddyInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    /**
     * Submits or updates a buddy rating for a shared event.
     *
     * @param raterId logged-in user ID
     * @param rateeId rated buddy user ID
     * @param eventId shared event ID
     * @param score 1..5 satisfaction score
     * @param wouldMeetAgain whether the rater wants to hang out again
     * @param comment optional short comment
     */
    public void execute(
            String raterId,
            String rateeId,
            String eventId,
            int score,
            boolean wouldMeetAgain,
            String comment) {
        interactor.execute(
                new RateBuddyInputData(raterId, rateeId, eventId, score, wouldMeetAgain, comment));
    }
}
