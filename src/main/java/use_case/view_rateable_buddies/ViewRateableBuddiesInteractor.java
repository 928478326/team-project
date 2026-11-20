package use_case.view_rateable_buddies;

import java.util.Objects;

/**
 * Interactor for discovering co-participants eligible for buddy ratings.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class ViewRateableBuddiesInteractor implements ViewRateableBuddiesInputBoundary {

    private final ViewRateableBuddiesDataAccessInterface dataAccess;
    private final ViewRateableBuddiesOutputBoundary outputBoundary;

    public ViewRateableBuddiesInteractor(
            ViewRateableBuddiesDataAccessInterface dataAccess,
            ViewRateableBuddiesOutputBoundary outputBoundary) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
    }

    @Override
    public void execute(ViewRateableBuddiesInputData inputData) {
        // TODO [Track C]:
        // 1. Validate userId non-blank.
        // 2. Load joined events, co-participants (excluding self), user display names,
        //    and existing ratings authored by userId.
        // 3. Sort deterministically (eventStartsAt DESC, eventId ASC, buddyUserId ASC)
        //    and present via outputBoundary.prepareSuccessView(...).
        outputBoundary.prepareFailView(
                "TODO [Track C]: Implement ViewRateableBuddiesInteractor.execute");
    }
}
