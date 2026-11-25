
package use_case.edit_schedule;

import entity.CourseEnrollment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Interactor implementing schedule loading,
 * adding/updating, and deletion.
 */
public final class EditScheduleInteractor
    implements EditScheduleInputBoundary {

    private final EditScheduleDataAccessInterface dataAccess;
    private final CourseCatalogGateway catalogGateway;
    private final EditScheduleOutputBoundary outputBoundary;

    public EditScheduleInteractor(
        EditScheduleDataAccessInterface dataAccess,
        CourseCatalogGateway catalogGateway,
        EditScheduleOutputBoundary outputBoundary) {
        this.dataAccess = Objects.requireNonNull(dataAccess);
        this.catalogGateway = Objects.requireNonNull(catalogGateway);
        this.outputBoundary = Objects.requireNonNull(outputBoundary);
    }

    @Override
    public void execute(EditScheduleInputData inputData) {
        String userId = inputData.getUserId();

        if (userId.isBlank()) {
            outputBoundary.prepareFailView(
                "User ID must not be blank.");
            return;
        }

        String statusMessage;

        try {
            switch (inputData.getAction()) {
                case LOAD:
                    statusMessage = "Schedule loaded.";
                    break;

                case ADD:
                    CourseEnrollment enrollment =
                        new CourseEnrollment(
                            userId,
                            inputData.getCourseCode(),
                            inputData.getLecSection(),
                            inputData.getTutSection());

                    dataAccess.saveEnrollment(enrollment);
                    statusMessage = "Course saved successfully.";
                    break;

                case REMOVE:
                    String courseCode =
                        CourseEnrollment.normalizeCourseCode(
                            inputData.getCourseCode());

                    dataAccess.deleteEnrollment(userId, courseCode);
                    statusMessage = "Course removed successfully.";
                    break;

                default:
                    outputBoundary.prepareFailView(
                        "Unsupported action.");
                    return;
            }
        } catch (IllegalArgumentException exception) {
            outputBoundary.prepareFailView(exception.getMessage());
            return;
        }

        List<CourseEnrollment> enrollments =
            dataAccess.findEnrollmentsByUser(userId);

        List<EditScheduleOutputData.EnrollmentItem> items =
            new ArrayList<>();

        for (CourseEnrollment enrollment : enrollments) {
            String courseTitle = catalogGateway
                .findCourseTitle(enrollment.getCourseCode())
                .orElse("");

            items.add(new EditScheduleOutputData.EnrollmentItem(
                enrollment.getCourseCode(),
                courseTitle,
                enrollment.getLecSection(),
                enrollment.getTutSection()));
        }

        outputBoundary.prepareSuccessView(
            new EditScheduleOutputData(items, statusMessage));
    }
}
