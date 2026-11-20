package use_case.edit_schedule;

/**
 * Input boundary for viewing, adding, or removing UofT course enrollments.
 */
public interface EditScheduleInputBoundary {

    /**
     * Executes a schedule operation (load, add/update, or remove).
     *
     * @param inputData schedule action and course fields
     */
    void execute(EditScheduleInputData inputData);
}
