package use_case.logout;

/**
 * Immutable input data for the Logout use case.
 */
public final class LogoutInputData {

    private final String username;

    public LogoutInputData(String username) {
        this.username = username == null ? "" : username;
    }

    public String getUsername() {
        return username;
    }
}
