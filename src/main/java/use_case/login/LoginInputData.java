package use_case.login;

/**
 * Immutable input data for the Login use case.
 */
public final class LoginInputData {

    private final String username;
    private final String password;

    public LoginInputData(String username, String password) {
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
