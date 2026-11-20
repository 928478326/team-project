package interface_adapter.account;

import java.util.Objects;
import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInputData;

/**
 * Controller for account registration.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class SignupController {

    private final SignupInputBoundary interactor;

    public SignupController(SignupInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    /**
     * Packages registration form fields and invokes the Signup use case.
     *
     * @param username chosen username
     * @param password chosen password
     * @param confirmPassword repeated password
     */
    public void execute(String username, String password, String confirmPassword) {
        SignupInputData inputData = new SignupInputData(username, password, confirmPassword);
        interactor.execute(inputData);
    }
}
