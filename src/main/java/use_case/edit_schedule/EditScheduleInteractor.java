package use_case.edit_schedule;

import java.util.Objects;

/**
 * Interactor implementing schedule loading, adding/updating, and deletion.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class EditScheduleInteractor implements EditScheduleInputBoundary {

    private final EditScheduleDataAccessInterface dataAccess;
    private final CourseCatalogGateway catalogGateway;
    private final EditScheduleOutputBoundary outputBoundary;

    public EditScheduleInteractor(
            EditScheduleDataAccessInterface dataAccess,
            CourseCatalogGateway catalogGateway,
            EditScheduleOutputBoundary outputBoundary) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.catalogGateway =
                Objects.requireNonNull(catalogGateway, "catalogGateway must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
    }

    @Override
    public void execute(EditScheduleInputData inputData) {
        // TODO [Track C]:
        // 1. Validate userId non-blank.
        // 2. Switch on inputData.getAction():
        //    - LOAD: read findEnrollmentsByUser(userId)
        //    - ADD: construct CourseEnrollment (catch IllegalArgumentException -> prepareFailView),
        //           saveEnrollment(enrollment), reload list
        //    - REMOVE: normalizeCourseCode, deleteEnrollment(userId, code), reload list
        // 3. Enrich each enrollment with catalogGateway.findCourseTitle(code).orElse("") and
        //    present via outputBoundary.prepareSuccessView(...).
        outputBoundary.prepareFailView("TODO [Track C]: Implement EditScheduleInteractor.execute");
    }
}
