package interface_adapter.course_match;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import use_case.match_course_buddies.MatchCourseBuddiesOutputBoundary;
import use_case.match_course_buddies.MatchCourseBuddiesOutputData;

/**
 * Presenter for study-buddy course matches.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class MatchCourseBuddiesPresenter implements MatchCourseBuddiesOutputBoundary {

    private final CourseMatchViewModel viewModel;

    public MatchCourseBuddiesPresenter(CourseMatchViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
    }

    @Override
    public void prepareSuccessView(MatchCourseBuddiesOutputData outputData) {
        List<CourseMatchState.MatchRow> rows = new ArrayList<>();
        for (MatchCourseBuddiesOutputData.MatchItem item : outputData.getMatches()) {
            rows.add(
                    new CourseMatchState.MatchRow(
                            item.getUserId(),
                            item.getDisplayName(),
                            item.getWeekendStyleLabel(),
                            item.getContactInfo(),
                            item.getMatchScore(),
                            item.getOverlapSummaries()));
        }
        CourseMatchState state = viewModel.getState();
        state.setFilterMode(outputData.getFilterMode());
        state.setMatches(rows);
        state.setErrorMessage("");
        state.setStatusMessage(outputData.getInfoMessage());
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
