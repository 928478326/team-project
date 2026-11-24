package use_case.signup;

import entity.PasswordHasher;
import entity.User;
import entity.UserFactory;
import java.util.Objects;
import java.util.UUID;

/**
 * Interactor implementing the Signup use case.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class SignupInteractor implements SignupInputBoundary {

    /** Minimum required password length. */
    public static final int MIN_PASSWORD_LENGTH = 6;

    private final SignupDataAccessInterface dataAccess;
    private final SignupOutputBoundary outputBoundary;
    private final UserFactory userFactory;
    private final PasswordHasher passwordHasher;

    public SignupInteractor(
            SignupDataAccessInterface dataAccess,
            SignupOutputBoundary outputBoundary,
            UserFactory userFactory,
            PasswordHasher passwordHasher) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.userFactory = Objects.requireNonNull(userFactory, "userFactory must not be null");
        this.passwordHasher =
                Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
    }

    @Override
    public void execute(SignupInputData inputData) {
        String password = inputData.getPassword();
        if (inputData.getUsername().isBlank()) {
            outputBoundary.prepareFailView("Enter a username.");
            return;
        }
        if (password.isBlank() || password.length() < MIN_PASSWORD_LENGTH) {
            outputBoundary.prepareFailView("Enter a password of at least " + MIN_PASSWORD_LENGTH + " characters.");
            return;
        }
        if (!password.equals(inputData.getConfirmPassword())) {
            outputBoundary.prepareFailView("The passwords do not match.");
            return;
        }

        User user = userFactory.create(UUID.randomUUID().toString(),
                User.normalizeUsername(inputData.getUsername()), passwordHasher.hash(password));
        boolean created;
        try {
            created = dataAccess.saveUser(user);
        } catch (IllegalStateException ex) {
            outputBoundary.prepareFailView("Could not save the account. Please try again.");
            return;
        }
        if (created) {
            outputBoundary.prepareSuccessView(new SignupOutputData(
                    user.getUserId(), user.getUsername(), user.getDisplayName()));
        } else {
            outputBoundary.prepareFailView("That username is already taken.");
        }
    }
}
