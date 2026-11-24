package interface_adapter.community;

import interface_adapter.ViewModel;

/**
 * ViewModel for the Community Events view.
 *
 * <p>Two kinds of change are fired so a list refresh never wipes out a message the student
 * still needs to read:
 * <ul>
 *   <li>{@link #STATE_PROPERTY}: the event list or community options changed.</li>
 *   <li>{@link #FEEDBACK_PROPERTY}: a new status or error message should be shown
 *       (after publishing, joining, or a failed load).</li>
 * </ul>
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CommunityEventsViewModel extends ViewModel<CommunityEventsState> {

    public static final String VIEW_NAME = "community_events";

    /** Property fired when {@link CommunityEventsState#getStatusMessage()} or the error changes. */
    public static final String FEEDBACK_PROPERTY = "feedback";

    public CommunityEventsViewModel() {
        super(VIEW_NAME);
        setState(new CommunityEventsState());
    }
}
