package use_case.signup;

import entity.PasswordHasher;
import entity.UserFactory;
import java.util.Objects;

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
        // TODO [Track A]:
        // 1. Validate username non-blank, password length >= MIN_PASSWORD_LENGTH,
        //    and password equals confirmPassword.
        // 2. Normalize username via User.normalizeUsername(...) and check existsByUsername(...).
        // 3. Generate UUID userId, hash password via passwordHasher.hash(...),
        //    build User via userFactory.create(...), save via dataAccess.saveUser(...).
        // 4. Call outputBoundary.prepareSuccessView(new SignupOutputData(...)).
        outputBoundary.prepareFailView("TODO [Track A]: Implement SignupInteractor.execute");
    }
}
