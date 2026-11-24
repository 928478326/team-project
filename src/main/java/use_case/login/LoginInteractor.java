package use_case.login;

import entity.PasswordHasher;
import entity.User;
import java.util.Objects;
import java.util.Optional;

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
        if (inputData.getUsername().isBlank() || inputData.getPassword().isBlank()) {
            outputBoundary.prepareFailView("Enter your username and password.");
            return;
        }

        Optional<User> account;
        try {
            account = dataAccess.findByUsername(User.normalizeUsername(inputData.getUsername()));
        } catch (IllegalStateException ex) {
            outputBoundary.prepareFailView("Could not load the account. Please try again.");
            return;
        }
        if (account.isEmpty()
                || !passwordHasher.matches(inputData.getPassword(), account.get().getPasswordHash())) {
            outputBoundary.prepareFailView("Incorrect username or password.");
            return;
        }
        User user = account.get();
        outputBoundary.prepareSuccessView(new LoginOutputData(
                user.getUserId(), user.getUsername(), user.getDisplayName()));
    }
}
