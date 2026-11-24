package interface_adapter.buddy_rating;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import use_case.rate_buddy.RateBuddyOutputData;

class BuddyRatingViewModelTest {

    @Test
    void lateSubmissionCannotReplaceFeedbackInTheNextSession() {
        final BuddyRatingViewModel model = new BuddyRatingViewModel();
        final RateBuddyPresenter presenter = new RateBuddyPresenter(model);
        final RateBuddyOutputData saved = new RateBuddyOutputData("peer", "event", 4, true, "Rating saved.");
        final Runnable oldRequest = model.forCurrentSession(() -> presenter.prepareSuccessView(saved));

        model.clearSession();
        final String currentError = "Could not save your rating.";
        model.forCurrentSession(() -> presenter.prepareFailView(currentError)).run();
        oldRequest.run();

        Assertions.assertEquals(currentError, model.getState().getErrorMessage());
        Assertions.assertTrue(model.getState().getStatusMessage().isEmpty());
    }
}
