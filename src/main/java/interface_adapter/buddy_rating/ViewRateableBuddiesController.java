package interface_adapter.buddy_rating;

import java.util.Objects;
import use_case.view_rateable_buddies.ViewRateableBuddiesInputBoundary;
import use_case.view_rateable_buddies.ViewRateableBuddiesInputData;

/**
 * Controller for loading buddies who shared events with the current user.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class ViewRateableBuddiesController {

    private final ViewRateableBuddiesInputBoundary interactor;

    public ViewRateableBuddiesController(ViewRateableBuddiesInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    public void execute(String userId) {
        interactor.execute(new ViewRateableBuddiesInputData(userId));
    }
}
