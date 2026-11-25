package interface_adapter.community;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import use_case.browse_events.BrowseEventsOutputBoundary;
import use_case.browse_events.BrowseEventsOutputData;

/**
 * Presenter for browsing community events.
 * Formats UTC timestamps into the configured local display zone and updates
 * {@link CommunityEventsViewModel}.
 *
 * <p>A successful load only replaces the list (fires {@link CommunityEventsViewModel#STATE_PROPERTY}),
 * so a "You joined..." message shown just before the refresh stays on screen.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class BrowseEventsPresenter implements BrowseEventsOutputBoundary {

    /** Example: {@code Thu Dec 3, 14:00}. English names regardless of the computer's language. */
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM d, HH:mm", Locale.ENGLISH);

    private final CommunityEventsViewModel viewModel;
    private final ZoneId displayZone;

    public BrowseEventsPresenter(CommunityEventsViewModel viewModel, ZoneId displayZone) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.displayZone = Objects.requireNonNull(displayZone, "displayZone must not be null");
    }

    @Override
    public void prepareSuccessView(BrowseEventsOutputData outputData) {
        final List<CommunityEventsState.EventRow> rows = new ArrayList<>();
        for (BrowseEventsOutputData.EventItem item : outputData.getEvents()) {
            final String formattedTime =
                    item.getStartsAt().atZone(displayZone).format(DISPLAY_FORMATTER);
            rows.add(
                    new CommunityEventsState.EventRow(
                            item.getEventId(),
                            item.getCommunityId(),
                            item.getCommunityName(),
                            item.getCreatorDisplayName(),
                            item.getTitle(),
                            item.getDescription(),
                            formattedTime,
                            item.getJoinedCount(),
                            item.getCapacity(),
                            item.isJoinedByCurrentUser(),
                            item.isOpenForJoin()));
        }
        final List<CommunityEventsState.CommunityOption> options = new ArrayList<>();
        for (BrowseEventsOutputData.CommunityOption option : outputData.getCommunities()) {
            options.add(new CommunityEventsState.CommunityOption(option.getCommunityId(), option.getName()));
        }
        final CommunityEventsState state = viewModel.getState();
        state.setSelectedCommunityId(outputData.getSelectedCommunityId());
        state.setEvents(rows);
        state.setCommunities(options);
        state.setClearCreateForm(false);
        viewModel.firePropertyChanged();
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
