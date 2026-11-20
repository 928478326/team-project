package use_case.logout;

/**
 * Immutable output data for the Logout use case.
 */
public final class LogoutOutputData {

    private final String loggedOutUsername;

    public LogoutOutputData(String loggedOutUsername) {
        this.loggedOutUsername = loggedOutUsername == null ? "" : loggedOutUsername;
    }

    public String getLoggedOutUsername() {
        return loggedOutUsername;
    }
}
