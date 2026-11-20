package interface_adapter.account;

/**
 * Mutable UI state for the Account (Login / Signup) screen.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class AccountState {

    private String loginUsername = "";
    private String signupUsername = "";
    private String statusMessage = "";
    private String errorMessage = "";

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername == null ? "" : loginUsername;
    }

    public String getSignupUsername() {
        return signupUsername;
    }

    public void setSignupUsername(String signupUsername) {
        this.signupUsername = signupUsername == null ? "" : signupUsername;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage == null ? "" : statusMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? "" : errorMessage;
    }
}
