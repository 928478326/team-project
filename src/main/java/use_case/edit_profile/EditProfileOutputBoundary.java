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

    /**
     * Presents a failure for a particular owner, allowing asynchronous adapters to drop stale results.
     * @param userId profile associated with this request
     * @param errorMessage user-facing error
     */
    default void prepareFailView(String userId, String errorMessage) {
        prepareFailView(errorMessage);
    }
}
