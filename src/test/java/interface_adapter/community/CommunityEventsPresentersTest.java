package interface_adapter.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import use_case.browse_events.BrowseEventsOutputData;
import use_case.create_event.CreateEventOutputData;
import use_case.join_event.JoinEventOutputData;

class CommunityEventsPresentersTest {

    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");

    private final CommunityEventsViewModel viewModel = new CommunityEventsViewModel();
    private final List<String> firedProperties = new ArrayList<>();

    CommunityEventsPresentersTest() {
        viewModel.addPropertyChangeListener(evt -> firedProperties.add(evt.getPropertyName()));
    }

    private static BrowseEventsOutputData oneEvent() {
        final BrowseEventsOutputData.EventItem item = new BrowseEventsOutputData.EventItem(
                "e-1", "study", "Study", "james", "Review", "Bring notes", 4, 4,
                Instant.parse("2026-12-03T19:00:00Z"), false, true);
        return new BrowseEventsOutputData("study", List.of(item),
                List.of(new BrowseEventsOutputData.CommunityOption("study", "Study")));
    }

    @Test
    void browseFormatsRowsInTorontoTimeAndKeepsTheCurrentMessage() {
        viewModel.getState().setStatusMessage("You joined the event.");

        new BrowseEventsPresenter(viewModel, TORONTO).prepareSuccessView(oneEvent());

        final CommunityEventsState state = viewModel.getState();
        assertEquals(List.of(CommunityEventsViewModel.STATE_PROPERTY), firedProperties);
        assertEquals("study", state.getSelectedCommunityId());
        assertEquals("Thu Dec 3, 14:00", state.getEvents().get(0).getStartsAtDisplay());
        assertTrue(state.getEvents().get(0).isFull());
        assertEquals("Study", state.getCommunities().get(0).toString());
        assertEquals("You joined the event.", state.getStatusMessage());
    }

    @Test
    void browseFailureIsShownAsFeedback() {
        new BrowseEventsPresenter(viewModel, TORONTO).prepareFailView("Unknown community: chess");

        assertEquals(List.of(CommunityEventsViewModel.FEEDBACK_PROPERTY), firedProperties);
        assertEquals("Unknown community: chess", viewModel.getState().getErrorMessage());
    }

    @Test
    void publishSuccessClearsTheFormAndFailureKeepsIt() {
        final CreateEventPresenter presenter = new CreateEventPresenter(viewModel);

        presenter.prepareFailView("Enter an event title.");
        assertFalse(viewModel.getState().isClearCreateForm());
        assertEquals("Enter an event title.", viewModel.getState().getErrorMessage());

        presenter.prepareSuccessView(new CreateEventOutputData("e-9", "food", "Hotpot", 5, Instant.EPOCH));
        assertTrue(viewModel.getState().isClearCreateForm());
        assertEquals("", viewModel.getState().getErrorMessage());
        assertTrue(viewModel.getState().getStatusMessage().contains("Hotpot"));
        assertEquals(List.of(CommunityEventsViewModel.FEEDBACK_PROPERTY, CommunityEventsViewModel.FEEDBACK_PROPERTY),
                firedProperties);
    }

    @Test
    void joinResultsAreShownAsFeedback() {
        final JoinEventPresenter presenter = new JoinEventPresenter(viewModel);

        presenter.prepareSuccessView(new JoinEventOutputData("e-1", false, 2, 4, "You joined the event."));
        assertEquals("You joined the event.", viewModel.getState().getStatusMessage());

        presenter.prepareFailView("This event is full.");
        assertEquals("", viewModel.getState().getStatusMessage());
        assertEquals("This event is full.", viewModel.getState().getErrorMessage());
        assertEquals(List.of(CommunityEventsViewModel.FEEDBACK_PROPERTY, CommunityEventsViewModel.FEEDBACK_PROPERTY),
                firedProperties);
    }
}
