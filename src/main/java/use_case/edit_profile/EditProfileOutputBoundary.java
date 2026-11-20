package use_case.edit_profile;

/**
 * Output boundary for loading and updating a student's profile.
 */
public interface EditProfileOutputBoundary {

    /**
     * Presents the loaded or saved profile fields.
     *
     * @param outputData profile DTOs
     */
    void prepareSuccessView(EditProfileOutputData outputData);

    /**
     * Presents a validation or storage error.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
