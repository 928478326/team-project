package use_case.login;

import entity.PasswordHasher;
import java.util.Objects;

/**
 * Interactor implementing the Login use case.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class LoginInteractor implements LoginInputBoundary {

    private final LoginDataAccessInterface dataAccess;
    private final LoginOutputBoundary outputBoundary;
    private final PasswordHasher passwordHasher;

    public LoginInteractor(
            LoginDataAccessInterface dataAccess,
            LoginOutputBoundary outputBoundary,
            PasswordHasher passwordHasher) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.passwordHasher =
                Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
    }

    @Override
    public void execute(LoginInputData inputData) {
        // TODO [Track A]:
        // 1. Validate non-blank username and password.
        // 2. Normalize username via User.normalizeUsername(...) and call findByUsername(...).
        // 3. Verify password via user.matchesPasswordHash(passwordHasher.hash(password)).
        // 4. On success call outputBoundary.prepareSuccessView(new LoginOutputData(...));
        //    otherwise call outputBoundary.prepareFailView(...).
        outputBoundary.prepareFailView("TODO [Track A]: Implement LoginInteractor.execute");
    }
}
