package interface_adapter.course_match;

import java.util.Objects;
import use_case.edit_schedule.EditScheduleInputBoundary;
import use_case.edit_schedule.EditScheduleInputData;

/**
 * Controller for loading, adding, and deleting course schedule enrollments.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class EditScheduleController {

    private final EditScheduleInputBoundary interactor;

    public EditScheduleController(EditScheduleInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    public void loadSchedule(String userId) {
        interactor.execute(
                new EditScheduleInputData(userId, EditScheduleInputData.Action.LOAD, "", "", ""));
    }

    public void addCourse(String userId, String courseCode, String lecSection, String tutSection) {
        interactor.execute(
                new EditScheduleInputData(
                        userId,
                        EditScheduleInputData.Action.ADD,
                        courseCode,
                        lecSection,
                        tutSection));
    }

    public void removeCourse(String userId, String courseCode) {
        interactor.execute(
                new EditScheduleInputData(
                        userId, EditScheduleInputData.Action.REMOVE, courseCode, "", ""));
    }
}
