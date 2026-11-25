package interface_adapter.community;

import java.util.Objects;

import use_case.join_event.JoinEventOutputBoundary;
import use_case.join_event.JoinEventOutputData;

/**
 * Presenter for joining a community event.
 *
 * <p>Only the message is updated here. The view reloads the list right after a join attempt,
 * so seat counts always come from storage rather than being patched in place.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class JoinEventPresenter implements JoinEventOutputBoundary {

    private final CommunityEventsViewModel viewModel;

    public JoinEventPresenter(CommunityEventsViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
    }

    @Override
    public void prepareSuccessView(JoinEventOutputData outputData) {
        final CommunityEventsState state = viewModel.getState();
        state.setErrorMessage("");
        state.setClearCreateForm(false);
        state.setStatusMessage(outputData.getMessage());
        viewModel.firePropertyChanged(CommunityEventsViewModel.FEEDBACK_PROPERTY);
    }

    @Override
    public void prepareFailView(String errorMessage) {
        final CommunityEventsState state = viewModel.getState();
        state.setStatusMessage("");
        state.setClearCreateForm(false);
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChanged(CommunityEventsViewModel.FEEDBACK_PROPERTY);
    }
}
