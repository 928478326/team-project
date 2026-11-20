package interface_adapter.account;

import java.util.Objects;
import use_case.login.LoginInputBoundary;
import use_case.login.LoginInputData;

/**
 * Controller for account login.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class LoginController {

    private final LoginInputBoundary interactor;

    public LoginController(LoginInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    /**
     * Packages login credentials and invokes the Login use case.
     *
     * @param username entered username
     * @param password entered password
     */
    public void execute(String username, String password) {
        LoginInputData inputData = new LoginInputData(username, password);
        interactor.execute(inputData);
    }
}
