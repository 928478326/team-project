package interface_adapter.community;

import java.util.Objects;

import use_case.create_event.CreateEventOutputBoundary;
import use_case.create_event.CreateEventOutputData;

/**
 * Presenter for creating a community event.
 *
 * <p>On success it asks the view to clear the form; on failure it keeps the form as typed so the
 * student can fix just the field that was wrong.
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
        final CommunityEventsState state = viewModel.getState();
        state.setErrorMessage("");
        state.setStatusMessage("Published \"" + outputData.getTitle() + "\" for up to "
                + outputData.getCapacity() + " people.");
        state.setClearCreateForm(true);
        viewModel.firePropertyChanged(CommunityEventsViewModel.FEEDBACK_PROPERTY);
    }

    @Override
    public void prepareFailView(String errorMessage) {
        final CommunityEventsState state = viewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        state.setClearCreateForm(false);
        viewModel.firePropertyChanged(CommunityEventsViewModel.FEEDBACK_PROPERTY);
    }
}
