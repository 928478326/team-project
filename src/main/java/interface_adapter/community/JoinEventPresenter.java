package interface_adapter.community;

import java.util.Objects;
import use_case.join_event.JoinEventOutputBoundary;
import use_case.join_event.JoinEventOutputData;

/**
 * Presenter for joining a community event.
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
        // TODO [Track B]: Update the matching EventRow in state (or trigger refresh) and show message.
        CommunityEventsState state = viewModel.getState();
        state.setErrorMessage("");
        state.setClearCreateForm(false);
        state.setStatusMessage(outputData.getMessage());
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        CommunityEventsState state = viewModel.getState();
        state.setStatusMessage("");
        state.setClearCreateForm(false);
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChanged();
    }
}
