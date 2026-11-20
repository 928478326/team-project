package interface_adapter;

/**
 * Immutable session state held by {@link SessionViewModel}.
 * Observed by the main shell and feature views so logout and profile-name changes
 * propagate without coupling Presenters across feature modules (OCP + Observer).
 */
public final class SessionState {

    private final boolean loggedIn;
    private final String userId;
    private final String username;
    private final String displayName;

    private SessionState(boolean loggedIn, String userId, String username, String displayName) {
        this.loggedIn = loggedIn;
        this.userId = userId == null ? "" : userId;
        this.username = username == null ? "" : username;
        this.displayName = displayName == null ? "" : displayName;
    }

    /**
     * Returns an unauthenticated session state.
     *
     * @return logged-out state
     */
    public static SessionState loggedOut() {
        return new SessionState(false, "", "", "");
    }

    /**
     * Returns an authenticated session state.
     *
     * @param userId active user ID
     * @param username active username
     * @param displayName active display name
     * @return logged-in state
     */
    public static SessionState loggedIn(String userId, String username, String displayName) {
        return new SessionState(true, userId, username, displayName);
    }

    /**
     * Returns a copy with an updated display name while keeping session credentials intact.
     *
     * @param newDisplayName updated display name
     * @return updated session state
     */
    public SessionState withDisplayName(String newDisplayName) {
        return new SessionState(this.loggedIn, this.userId, this.username, newDisplayName);
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }
}
