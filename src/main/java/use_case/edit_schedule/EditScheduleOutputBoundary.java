package use_case.edit_schedule;

/**
 * Output boundary for schedule management.
 */
public interface EditScheduleOutputBoundary {

    /**
     * Presents the refreshed course schedule list.
     *
     * @param outputData updated enrollments and status message
     */
    void prepareSuccessView(EditScheduleOutputData outputData);

    /**
     * Presents a validation or persistence error.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
