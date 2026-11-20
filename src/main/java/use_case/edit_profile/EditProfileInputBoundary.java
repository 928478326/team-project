package use_case.edit_profile;

/**
 * Input boundary for loading and updating a student's weekend style and interest profile.
 */
public interface EditProfileInputBoundary {

    /**
     * Loads or updates a student's profile based on {@code inputData.getAction()}.
     *
     * @param inputData profile load/save payload
     */
    void execute(EditProfileInputData inputData);
}
