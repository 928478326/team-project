package interface_adapter.recommendation;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import java.util.Objects;
import use_case.edit_profile.EditProfileOutputBoundary;
import use_case.edit_profile.EditProfileOutputData;

/**
 * Presenter for profile load and update results.
 * Updates {@link RecommendationViewModel} and synchronizes the active display name in
 * {@link SessionViewModel} via Observer.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class EditProfilePresenter implements EditProfileOutputBoundary {

    private final RecommendationViewModel viewModel;
    private final SessionViewModel sessionViewModel;

    public EditProfilePresenter(
            RecommendationViewModel viewModel, SessionViewModel sessionViewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.sessionViewModel =
                Objects.requireNonNull(sessionViewModel, "sessionViewModel must not be null");
    }

    @Override
    public void prepareSuccessView(EditProfileOutputData outputData) {
        RecommendationState state = viewModel.getState();
        state.setDisplayName(outputData.getDisplayName());
        state.setBio(outputData.getBio());
        state.setContactInfo(outputData.getContactInfo());
        state.setWeekendStyleCode(outputData.getWeekendStyleCode());
        state.setInterestedCommunityIds(outputData.getInterestedCommunityIds());
        state.setErrorMessage("");
        state.setStatusMessage(outputData.getStatusMessage());
        viewModel.firePropertyChanged();

        SessionState currentSession = sessionViewModel.getState();
        if (currentSession.isLoggedIn()
                && currentSession.getUserId().equals(outputData.getUserId())
                && !currentSession.getDisplayName().equals(outputData.getDisplayName())) {
            sessionViewModel.setState(currentSession.withDisplayName(outputData.getDisplayName()));
            sessionViewModel.firePropertyChanged();
        }
    }

    @Override
    public void prepareFailView(String errorMessage) {
        RecommendationState state = viewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChanged();
    }
}
