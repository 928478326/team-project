package interface_adapter.buddy_rating;

import java.util.Objects;
import use_case.rate_buddy.RateBuddyOutputBoundary;
import use_case.rate_buddy.RateBuddyOutputData;

/**
 * Presenter for buddy rating submissions.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class RateBuddyPresenter implements RateBuddyOutputBoundary {

    private final BuddyRatingViewModel viewModel;

    public RateBuddyPresenter(BuddyRatingViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
    }

    @Override
    public void prepareSuccessView(RateBuddyOutputData outputData) {
        BuddyRatingState state = viewModel.getState();
        state.setErrorMessage("");
        state.setStatusMessage(outputData.getMessage());
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        BuddyRatingState state = viewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChanged();
    }
}
