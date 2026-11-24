package interface_adapter.buddy_rating;

import java.util.Objects;

import use_case.rate_buddy.RateBuddyOutputBoundary;
import use_case.rate_buddy.RateBuddyOutputData;

/**
 * Presents the outcome of saving a rating while retaining the available buddy rows.
 */
public final class RateBuddyPresenter implements RateBuddyOutputBoundary {

    private final BuddyRatingViewModel viewModel;

    public RateBuddyPresenter(BuddyRatingViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
    }

    @Override
    public void prepareSuccessView(RateBuddyOutputData outputData) {
        viewModel.present(BuddyRatingViewModel.STATE_PROPERTY, state -> {
            state.setErrorMessage("");
            state.setStatusMessage(outputData.getMessage());
        });
    }

    @Override
    public void prepareFailView(String errorMessage) {
        viewModel.present(BuddyRatingViewModel.STATE_PROPERTY, state -> {
            state.setStatusMessage("");
            state.setErrorMessage(errorMessage);
        });
    }
}
