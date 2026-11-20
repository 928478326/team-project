package use_case.signup;

/**
 * Immutable output data for a successful account signup.
 */
public final class SignupOutputData {

    private final String userId;
    private final String username;
    private final String displayName;

    public SignupOutputData(String userId, String username, String displayName) {
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
