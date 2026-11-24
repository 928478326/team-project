package use_case.view_rateable_buddies;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import use_case.view_rateable_buddies.ViewRateableBuddiesOutputData.RateableBuddyItem;

/**
 * Interactor for discovering co-participants eligible for buddy ratings.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class ViewRateableBuddiesInteractor implements ViewRateableBuddiesInputBoundary {

    private static final Comparator<RateableBuddyItem> DISPLAY_ORDER =
            Comparator.comparing(RateableBuddyItem::getEventStartsAt).reversed()
                    .thenComparing(RateableBuddyItem::getEventId)
                    .thenComparing(RateableBuddyItem::getBuddyUserId);

    private final Clock clock;
    private final ViewRateableBuddiesDataAccessInterface dataAccess;
    private final ViewRateableBuddiesOutputBoundary outputBoundary;

    public ViewRateableBuddiesInteractor(
            ViewRateableBuddiesDataAccessInterface dataAccess,
            ViewRateableBuddiesOutputBoundary outputBoundary) {
        this(dataAccess, outputBoundary, Clock.systemUTC());
    }

    public ViewRateableBuddiesInteractor(ViewRateableBuddiesDataAccessInterface dataAccess,
            ViewRateableBuddiesOutputBoundary outputBoundary, Clock clock) {
        this.clock = Objects.requireNonNull(clock);
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
    }

    @Override
    public void execute(ViewRateableBuddiesInputData inputData) {
        if (inputData.getUserId().isBlank()) {
            outputBoundary.prepareFailView("Sign in to see your meetup buddies.");
        }
        else {
            loadBuddies(inputData.getUserId());
        }
    }

    private void loadBuddies(String userId) {
        List<RateableBuddyItem> items = null;
        try {
            items = new ArrayList<>(dataAccess.findRateableBuddies(userId, clock.instant()));
        }
        catch (IllegalStateException unavailable) {
            outputBoundary.prepareFailView("Could not load your meetup buddies. Please try again.");
        }
        if (items != null) {
            items.sort(DISPLAY_ORDER);
            outputBoundary.prepareSuccessView(new ViewRateableBuddiesOutputData(items));
        }
    }
}
