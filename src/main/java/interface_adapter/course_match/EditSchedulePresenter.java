package interface_adapter.course_match;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import use_case.edit_schedule.EditScheduleOutputBoundary;
import use_case.edit_schedule.EditScheduleOutputData;

/**
 * Presenter for schedule management operations.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class EditSchedulePresenter implements EditScheduleOutputBoundary {

    private final CourseMatchViewModel viewModel;

    public EditSchedulePresenter(CourseMatchViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
    }

    @Override
    public void prepareSuccessView(EditScheduleOutputData outputData) {
        List<CourseMatchState.EnrollmentRow> rows = new ArrayList<>();
        for (EditScheduleOutputData.EnrollmentItem item : outputData.getEnrollments()) {
            rows.add(
                    new CourseMatchState.EnrollmentRow(
                            item.getCourseCode(),
                            item.getCourseTitle(),
                            item.getLecSection(),
                            item.getTutSection()));
        }
        CourseMatchState state = viewModel.getState();
        state.setEnrollments(rows);
        state.setErrorMessage("");
        state.setStatusMessage(outputData.getStatusMessage());
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        CourseMatchState state = viewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChanged();
    }
}
