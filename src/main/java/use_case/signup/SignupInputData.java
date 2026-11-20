package use_case.signup;

/**
 * Immutable input data for the Signup use case.
 */
public final class SignupInputData {

    private final String username;
    private final String password;
    private final String confirmPassword;

    public SignupInputData(String username, String password, String confirmPassword) {
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
        this.confirmPassword = confirmPassword == null ? "" : confirmPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}
