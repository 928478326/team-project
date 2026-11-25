package use_case.login;

import java.util.Objects;
import java.util.Optional;

import entity.PasswordHasher;
import entity.User;

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
        }
        else {
            authenticate(User.normalizeUsername(inputData.getUsername()), inputData.getPassword());
        }
    }

    private void authenticate(String username, String password) {
        Optional<User> authenticated = Optional.empty();
        String failure = "Incorrect username or password.";
        try {
            authenticated = dataAccess.findByUsername(username)
                    .filter(user -> passwordHasher.matches(password, user.getPasswordHash()));
        }
        catch (IllegalStateException ex) {
            failure = "Could not load the account. Please try again.";
        }
        if (authenticated.isPresent()) {
            final User user = authenticated.get();
            outputBoundary.prepareSuccessView(new LoginOutputData(
                    user.getUserId(), user.getUsername(), user.getDisplayName()));
        }
        else {
            outputBoundary.prepareFailView(failure);
        }
    }
}
