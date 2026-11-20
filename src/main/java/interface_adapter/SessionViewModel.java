package interface_adapter;

/**
 * Shared observable ViewModel holding the currently authenticated user's session metadata.
 */
public final class SessionViewModel extends ViewModel<SessionState> {

    public static final String SESSION_VIEW_NAME = "session";

    public SessionViewModel() {
        super(SESSION_VIEW_NAME);
        setState(SessionState.loggedOut());
    }
}
