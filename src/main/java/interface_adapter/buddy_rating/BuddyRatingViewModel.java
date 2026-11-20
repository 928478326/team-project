package interface_adapter.buddy_rating;

import interface_adapter.ViewModel;

/**
 * ViewModel for the Buddy Rating view.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class BuddyRatingViewModel extends ViewModel<BuddyRatingState> {

    public static final String VIEW_NAME = "buddy_rating";

    public BuddyRatingViewModel() {
        super(VIEW_NAME);
        setState(new BuddyRatingState());
    }
}
