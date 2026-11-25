package interface_adapter.recommendation;

import java.util.Objects;
import java.util.concurrent.Executor;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
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
    private final Executor onUiThread;
    private String sessionUserId;

    public EditProfilePresenter(
            RecommendationViewModel viewModel, SessionViewModel sessionViewModel) {
        this(viewModel, sessionViewModel, Runnable::run);
    }

    /**
     * Dispatches profile and session events on the same UI thread.
     * @param viewModel profile view model
     * @param sessionViewModel authenticated session
     * @param onUiThread executor used for view model updates
     */
    public EditProfilePresenter(RecommendationViewModel viewModel, SessionViewModel sessionViewModel,
            Executor onUiThread) {
        this.onUiThread = Objects.requireNonNull(onUiThread);
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.sessionViewModel =
                Objects.requireNonNull(sessionViewModel, "sessionViewModel must not be null");
        sessionUserId = sessionViewModel.getState().getUserId();
        sessionViewModel.addPropertyChangeListener(event -> onUiThread.execute(this::clearOnAccountChange));
    }

    private void clearOnAccountChange() {
        final String currentUserId = sessionViewModel.getState().getUserId();
        if (!currentUserId.equals(sessionUserId)) {
            sessionUserId = currentUserId;
            viewModel.setState(new RecommendationState());
            viewModel.firePropertyChanged();
        }
    }

    private boolean isCurrentUser(String userId) {
        final SessionState session = sessionViewModel.getState();
        return session.isLoggedIn() && session.getUserId().equals(userId);
    }

    @Override
    public void prepareSuccessView(EditProfileOutputData outputData) {
        onUiThread.execute(() -> {
            if (isCurrentUser(outputData.getUserId())) {
                clearOnAccountChange();
                synchronizeDisplayName(outputData);
                // The shell can reload the active tab while handling the name change.
                if (isCurrentUser(outputData.getUserId())) {
                    showProfile(outputData);
                }
            }
        });
    }

    private void synchronizeDisplayName(EditProfileOutputData outputData) {
        final SessionState currentSession = sessionViewModel.getState();
        if (!currentSession.getDisplayName().equals(outputData.getDisplayName())) {
            sessionViewModel.setState(currentSession.withDisplayName(outputData.getDisplayName()));
            sessionViewModel.firePropertyChanged();
        }
    }

    private void showProfile(EditProfileOutputData outputData) {
        final RecommendationState state = viewModel.getState();
        state.setDisplayName(outputData.getDisplayName());
        state.setBio(outputData.getBio());
        state.setContactInfo(outputData.getContactInfo());
        state.setWeekendStyleCode(outputData.getWeekendStyleCode());
        state.setInterestedCommunityIds(outputData.getInterestedCommunityIds());
        state.setPrivateStatus(outputData.getUtorid(), outputData.hasUtorid(),
                outputData.isShareContactInfo(), outputData.isUoftVerified());
        state.setErrorMessage("");
        state.setStatusMessage(outputData.getStatusMessage());
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        prepareFailView(sessionViewModel.getState().getUserId(), errorMessage);
    }

    @Override
    public void prepareFailView(String userId, String errorMessage) {
        onUiThread.execute(() -> {
            if (isCurrentUser(userId)) {
                showError(errorMessage);
            }
        });
    }

    private void showError(String errorMessage) {
        clearOnAccountChange();
        final RecommendationState state = viewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChanged();
    }
}
