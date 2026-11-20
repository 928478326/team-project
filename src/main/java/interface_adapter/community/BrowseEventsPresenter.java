package interface_adapter.community;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import use_case.browse_events.BrowseEventsOutputBoundary;
import use_case.browse_events.BrowseEventsOutputData;

/**
 * Presenter for browsing community events.
 * Formats UTC timestamps into the configured local display zone and updates
 * {@link CommunityEventsViewModel}.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class BrowseEventsPresenter implements BrowseEventsOutputBoundary {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CommunityEventsViewModel viewModel;
    private final ZoneId displayZone;

    public BrowseEventsPresenter(CommunityEventsViewModel viewModel, ZoneId displayZone) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.displayZone = Objects.requireNonNull(displayZone, "displayZone must not be null");
    }

    @Override
    public void prepareSuccessView(BrowseEventsOutputData outputData) {
        // TODO [Track B]: Refine formatting or empty-state messaging if desired.
        List<CommunityEventsState.EventRow> rows = new ArrayList<>();
        for (BrowseEventsOutputData.EventItem item : outputData.getEvents()) {
            String formattedTime =
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
        CommunityEventsState state = viewModel.getState();
        state.setSelectedCommunityId(outputData.getSelectedCommunityId());
        state.setEvents(rows);
        state.setErrorMessage("");
        state.setClearCreateForm(false);
        state.setStatusMessage(rows.isEmpty() ? "No events in this community yet." : "");
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        CommunityEventsState state = viewModel.getState();
        state.setErrorMessage(errorMessage);
        state.setClearCreateForm(false);
        viewModel.firePropertyChanged();
    }
}
