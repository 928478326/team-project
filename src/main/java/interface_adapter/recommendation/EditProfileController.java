package interface_adapter.recommendation;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import use_case.edit_profile.EditProfileInputBoundary;
import use_case.edit_profile.EditProfileInputData;

/**
 * Controller for loading and saving a student's weekend style and interest profile.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class EditProfileController {

    private final EditProfileInputBoundary interactor;

    public EditProfileController(EditProfileInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    public void loadProfile(String userId) {
        interactor.execute(
                new EditProfileInputData(
                        userId,
                        EditProfileInputData.Action.LOAD,
                        "",
                        "",
                        "",
                        "",
                        Collections.emptySet()));
    }

    public void saveProfile(
            String userId,
            String displayName,
            String bio,
            String contactInfo,
            String weekendStyleCode,
            Set<String> interestedCommunityIds) {
        interactor.execute(
                new EditProfileInputData(
                        userId,
                        EditProfileInputData.Action.SAVE,
                        displayName,
                        bio,
                        contactInfo,
                        weekendStyleCode,
                        interestedCommunityIds));
    }
}
