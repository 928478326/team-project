package interface_adapter.buddy_rating;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import use_case.view_rateable_buddies.ViewRateableBuddiesOutputBoundary;
import use_case.view_rateable_buddies.ViewRateableBuddiesOutputData;

/**
 * Presenter for viewing rateable buddies.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class ViewRateableBuddiesPresenter implements ViewRateableBuddiesOutputBoundary {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BuddyRatingViewModel viewModel;
    private final ZoneId displayZone;

    public ViewRateableBuddiesPresenter(BuddyRatingViewModel viewModel, ZoneId displayZone) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.displayZone = Objects.requireNonNull(displayZone, "displayZone must not be null");
    }

    @Override
    public void prepareSuccessView(ViewRateableBuddiesOutputData outputData) {
        List<BuddyRatingState.RateableBuddyRow> rows = new ArrayList<>();
        for (ViewRateableBuddiesOutputData.RateableBuddyItem item : outputData.getItems()) {
            String formattedTime =
                    item.getEventStartsAt().atZone(displayZone).format(DISPLAY_FORMATTER);
            rows.add(
                    new BuddyRatingState.RateableBuddyRow(
                            item.getEventId(),
                            item.getEventTitle(),
                            formattedTime,
                            item.getBuddyUserId(),
                            item.getBuddyDisplayName(),
                            item.isAlreadyRated(),
                            item.getExistingScore(),
                            item.isExistingWouldMeetAgain(),
                            item.getExistingComment()));
        }
        BuddyRatingState state = viewModel.getState();
        state.setItems(rows);
        state.setErrorMessage("");
        state.setStatusMessage(
                rows.isEmpty() ? "Join an event with other students to rate buddies." : "");
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
