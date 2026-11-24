package use_case.edit_profile;

import java.util.Objects;
import java.util.Optional;

import entity.LlmPersona;
import entity.PrivateProfile;
import entity.PublicProfile;
import entity.User;
import entity.WeekendStyle;

/**
 * Interactor implementing student profile loading, 3-tier visibility updates, and PII-scrubbed
 * LLM persona enrichment on profile save.
 *
 * <p><b>Owner: Track D (Profile &amp; LLM Persona Lead)</b>
 */
public final class EditProfileInteractor implements EditProfileInputBoundary {

    private final EditProfileDataAccessInterface dataAccess;
    private final EditProfileOutputBoundary outputBoundary;
    private final LlmProfileEnrichmentGateway personaGateway;

    /**
     * Constructs an {@code EditProfileInteractor} using the default no-op persona gateway.
     *
     * @param dataAccess profile persistence port (non-null)
     * @param outputBoundary profile presenter port (non-null)
     */
    public EditProfileInteractor(
            EditProfileDataAccessInterface dataAccess, EditProfileOutputBoundary outputBoundary) {
        this(dataAccess, outputBoundary, LlmProfileEnrichmentGateway.identity());
    }

    /**
     * Constructs an {@code EditProfileInteractor} wired with an explicit LLM persona enrichment
     * gateway.
     *
     * @param dataAccess profile persistence port (non-null)
     * @param outputBoundary profile presenter port (non-null)
     * @param personaGateway LLM hidden-persona enrichment port (non-null)
     */
    public EditProfileInteractor(
            EditProfileDataAccessInterface dataAccess,
            EditProfileOutputBoundary outputBoundary,
            LlmProfileEnrichmentGateway personaGateway) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.personaGateway =
                Objects.requireNonNull(personaGateway, "personaGateway must not be null");
    }

    @Override
    public void execute(EditProfileInputData inputData) {
        if (inputData == null || inputData.getUserId().isEmpty()) {
            outputBoundary.prepareFailView("User ID must not be blank.");
        }
        else {
            Optional<User> existing = Optional.empty();
            String loadFailure = "User not found: " + inputData.getUserId();
            try {
                existing = dataAccess.findUserById(inputData.getUserId());
            }
            catch (IllegalStateException ex) {
                loadFailure = "Could not load the profile. Please try again.";
            }
            if (existing.isEmpty()) {
                outputBoundary.prepareFailView(loadFailure);
            }
            else if (inputData.getAction() == EditProfileInputData.Action.LOAD) {
                outputBoundary.prepareSuccessView(
                        toOutputData(existing.get(), "Profile loaded."));
            }
            else {
                handleSave(existing.get(), inputData);
            }
        }
    }

    private void handleSave(User currentUser, EditProfileInputData inputData) {
        EditProfileOutputData savedProfile = null;
        try {
            final WeekendStyle weekendStyle = WeekendStyle.of(inputData.getWeekendStyleCode());
            final PublicProfile updatedPublic = PublicProfile.createValidated(
                    inputData.getDisplayName(),
                    inputData.getBio(),
                    inputData.getContactInfo(),
                    weekendStyle,
                    inputData.getInterestedCommunityIds());
            final PrivateProfile updatedPrivate = resolvePrivateProfile(currentUser, inputData);

            final User withVisibleEdits =
                    currentUser.withEditableProfile(updatedPublic, updatedPrivate);
            final LlmPersona enrichedPersona = enrichPersonaSafely(withVisibleEdits);
            final User finalUser = withVisibleEdits.withLlmPersona(enrichedPersona);

            dataAccess.updateUserProfile(finalUser);
            savedProfile = toOutputData(finalUser, "Profile saved.");
        }
        catch (IllegalArgumentException ex) {
            outputBoundary.prepareFailView(ex.getMessage());
        }
        catch (IllegalStateException ex) {
            outputBoundary.prepareFailView("Could not save the profile. Please try again.");
        }
        if (savedProfile != null) {
            outputBoundary.prepareSuccessView(savedProfile);
        }
    }

    private static PrivateProfile resolvePrivateProfile(
            User currentUser, EditProfileInputData inputData) {
        final PrivateProfile resolved;
        if (inputData.isPrivateProfileIncluded()) {
            resolved = currentUser
                    .getPrivateProfile()
                    .withUserInput(inputData.getUtorid(), inputData.isShareContactInfo());
        }
        else {
            resolved = currentUser.getPrivateProfile();
        }
        return resolved;
    }

    private LlmPersona enrichPersonaSafely(User updatedUser) {
        LlmPersona resolved = updatedUser.getLlmPersona();
        final LlmProfileEnrichmentGateway.SanitizedProfileSnapshot snapshot =
                new LlmProfileEnrichmentGateway.SanitizedProfileSnapshot(
                        updatedUser.getDisplayName(),
                        updatedUser.getBio(),
                        updatedUser.getWeekendStyle().getDisplayName(),
                        updatedUser.getInterestedCommunityIds(),
                        updatedUser.getLlmPersona());
        try {
            final LlmPersona candidate = personaGateway.enrichPersona(snapshot);
            if (candidate != null) {
                resolved = candidate;
            }
        }
        catch (IllegalStateException | IllegalArgumentException ex) {
            resolved = updatedUser.getLlmPersona();
        }
        return resolved;
    }

    private static EditProfileOutputData toOutputData(User user, String statusMessage) {
        final PrivateProfile privateProfile = user.getPrivateProfile();
        final EditProfileOutputData base = new EditProfileOutputData(
                user.getUserId(),
                user.getDisplayName(),
                user.getBio(),
                user.getContactInfo(),
                user.getWeekendStyle().name(),
                user.getInterestedCommunityIds(),
                statusMessage);
        return base.withPrivateStatus(
                privateProfile.getUtorid(),
                user.hasUtorid(),
                privateProfile.isShareContactInfo(),
                user.isUoftVerified());
    }
}
