package use_case.login;

/**
 * Immutable output data for a successful login.
 */
public final class LoginOutputData {

    private final String userId;
    private final String username;
    private final String displayName;

    public LoginOutputData(String userId, String username, String displayName) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
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
