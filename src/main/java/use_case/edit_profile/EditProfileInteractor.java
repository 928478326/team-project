package use_case.edit_profile;

import java.util.Objects;

/**
 * Interactor implementing profile loading and editing.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class EditProfileInteractor implements EditProfileInputBoundary {

    private final EditProfileDataAccessInterface dataAccess;
    private final EditProfileOutputBoundary outputBoundary;

    public EditProfileInteractor(
            EditProfileDataAccessInterface dataAccess, EditProfileOutputBoundary outputBoundary) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
    }

    @Override
    public void execute(EditProfileInputData inputData) {
        // TODO [Track D]:
        // 1. Load user via dataAccess.findUserById(userId); fail if missing.
        // 2. If action == LOAD, present current user fields via prepareSuccessView.
        // 3. If action == SAVE, parse WeekendStyle.fromCode(...), build updatedUser via
        //    user.withProfile(...), call dataAccess.updateUserProfile(updatedUser),
        //    and present via prepareSuccessView (catch IllegalArgumentException -> prepareFailView).
        outputBoundary.prepareFailView("TODO [Track D]: Implement EditProfileInteractor.execute");
    }
}
