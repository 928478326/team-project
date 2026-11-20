package interface_adapter.community;

import java.util.Objects;
import use_case.create_event.CreateEventOutputBoundary;
import use_case.create_event.CreateEventOutputData;

/**
 * Presenter for creating a community event.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CreateEventPresenter implements CreateEventOutputBoundary {

    private final CommunityEventsViewModel viewModel;

    public CreateEventPresenter(CommunityEventsViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
    }

    @Override
    public void prepareSuccessView(CreateEventOutputData outputData) {
        // TODO [Track B]: Update CommunityEventsState status, signal form clear, and fire event.
        CommunityEventsState state = viewModel.getState();
        state.setErrorMessage("");
        state.setStatusMessage("Published event: " + outputData.getTitle());
        state.setClearCreateForm(true);
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        CommunityEventsState state = viewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        state.setClearCreateForm(false);
        viewModel.firePropertyChanged();
    }
}
