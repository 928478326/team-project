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

    /**
     * Requests the owner profile.
     * @param userId signed-in user ID
     */
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

    /**
     * Saves edits to the public profile.
     * @param userId signed-in user ID
     * @param displayName public display name
     * @param bio public biography
     * @param contactInfo owner contact handle
     * @param weekendStyleCode preset or free-text weekend preference
     * @param interestedCommunityIds selected communities
     */
    public void saveProfile(
            String userId,
            String displayName,
            String bio,
            String contactInfo,
            String weekendStyleCode,
            Set<String> interestedCommunityIds) {
        interactor.execute(publicEdits(userId, displayName, bio, contactInfo,
                weekendStyleCode, interestedCommunityIds));
    }

    /**
     * Saves edits to the public profile and private settings.
     * @param userId signed-in user ID
     * @param displayName public display name
     * @param bio public biography
     * @param contactInfo owner contact handle
     * @param weekendStyleCode preset or free-text weekend preference
     * @param interestedCommunityIds selected communities
     * @param settings self-entered identifier and contact visibility
     */
    public void saveProfile(
            String userId,
            String displayName,
            String bio,
            String contactInfo,
            String weekendStyleCode,
            Set<String> interestedCommunityIds,
            PrivateSettingsInput settings) {
        final EditProfileInputData edits = publicEdits(userId, displayName, bio, contactInfo,
                weekendStyleCode, interestedCommunityIds);
        interactor.execute(edits.withPrivateSettings(settings.getUtorid(), settings.isShareContactInfo()));
    }

    private static EditProfileInputData publicEdits(String userId, String displayName, String bio,
            String contactInfo, String weekendStyleCode, Set<String> interestedCommunityIds) {
        return new EditProfileInputData(userId, EditProfileInputData.Action.SAVE, displayName,
                bio, contactInfo, weekendStyleCode, interestedCommunityIds);
    }
}
