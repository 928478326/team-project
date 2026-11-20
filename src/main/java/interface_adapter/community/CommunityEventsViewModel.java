package interface_adapter.community;

import interface_adapter.ViewModel;

/**
 * ViewModel for the Community Events view.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CommunityEventsViewModel extends ViewModel<CommunityEventsState> {

    public static final String VIEW_NAME = "community_events";

    public CommunityEventsViewModel() {
        super(VIEW_NAME);
        setState(new CommunityEventsState());
    }
}
