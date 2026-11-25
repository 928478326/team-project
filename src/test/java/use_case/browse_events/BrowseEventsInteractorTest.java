package use_case.browse_events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import data_access.in_memory.InMemoryCampusBuddyStore;
import data_access.in_memory.InMemoryEventDataAccess;
import entity.CommonUserFactory;
import entity.Community;
import entity.Event;
import entity.Participation;
import use_case.browse_events.BrowseEventsOutputData.EventItem;

class BrowseEventsInteractorTest {

    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");
    private static final Instant NOW = at(2026, 11, 24, 12);

    private final InMemoryCampusBuddyStore store = new InMemoryCampusBuddyStore();
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final BrowseEventsInteractor interactor = new BrowseEventsInteractor(
            new InMemoryEventDataAccess(store), presenter, Clock.fixed(NOW, TORONTO));

    BrowseEventsInteractorTest() {
        store.putUser(new CommonUserFactory().create("u-james", "james", "hash"));
        // e-1 and e-2 start at the same moment, so they must be ordered by event ID.
        store.putEvent(new Event("e-2", Community.STUDY_ID, "u-james", "Review", "", 4, at(2026, 12, 3, 14)));
        store.putEvent(new Event("e-1", Community.GAMING_ID, "u-james", "Catan", "", 3, at(2026, 12, 3, 14)));
        store.putEvent(new Event("e-3", Community.STUDY_ID, "u-gone", "Problem set", "", 2, at(2026, 12, 1, 10)));
        store.putEvent(new Event("e-past", Community.FOOD_ID, "u-james", "Hotpot", "", 5, at(2026, 11, 15, 16)));
        store.putParticipation(new Participation("u-viewer", "e-2", NOW));
        store.putParticipation(new Participation("u-other", "e-2", NOW));
    }

    private static Instant at(int year, int month, int day, int hour) {
        return ZonedDateTime.of(year, month, day, hour, 0, 0, 0, TORONTO).toInstant();
    }

    private static List<String> ids(List<EventItem> items) {
        final List<String> ids = new ArrayList<>();
        for (EventItem item : items) {
            ids.add(item.getEventId());
        }
        return ids;
    }

    private EventItem item(String eventId) {
        for (EventItem item : presenter.success.getEvents()) {
            if (item.getEventId().equals(eventId)) {
                return item;
            }
        }
        throw new AssertionError("No row for " + eventId);
    }

    @Test
    void allCommunitiesAreListedByStartTimeThenEventId() {
        interactor.execute(new BrowseEventsInputData("u-viewer", BrowseEventsInputData.ALL_COMMUNITIES));

        assertNull(presenter.error);
        assertEquals(BrowseEventsInputData.ALL_COMMUNITIES, presenter.success.getSelectedCommunityId());
        assertEquals(List.of("e-past", "e-3", "e-1", "e-2"), ids(presenter.success.getEvents()));
    }

    @Test
    void communityOptionsAreIncludedForTheFilterAndCreateForm() {
        interactor.execute(new BrowseEventsInputData("u-viewer", "food"));

        final List<String> names = new ArrayList<>();
        for (BrowseEventsOutputData.CommunityOption option : presenter.success.getCommunities()) {
            names.add(option.getCommunityId() + "=" + option.getName());
        }
        assertEquals(List.of("study=Study", "sports=Sports", "gaming=Gaming", "food=Food", "chill=Chill"), names);
    }

    @Test
    void blankFilterMeansAllCommunities() {
        interactor.execute(new BrowseEventsInputData("u-viewer", "  "));

        assertEquals(BrowseEventsInputData.ALL_COMMUNITIES, presenter.success.getSelectedCommunityId());
        assertEquals(4, presenter.success.getEvents().size());
    }

    @ParameterizedTest
    @CsvSource({"study", "STUDY", "' Study '"})
    void filterIsCaseInsensitive(String filter) {
        interactor.execute(new BrowseEventsInputData("u-viewer", filter));

        assertNull(presenter.error);
        assertEquals("study", presenter.success.getSelectedCommunityId());
        assertEquals(List.of("e-3", "e-2"), ids(presenter.success.getEvents()));
    }

    @Test
    void rowsDescribeSeatsMembershipAndOpenStatus() {
        interactor.execute(new BrowseEventsInputData("u-viewer", "ALL"));

        final EventItem joined = item("e-2");
        assertEquals("Study", joined.getCommunityName());
        assertEquals("james", joined.getCreatorDisplayName());
        assertEquals(2, joined.getJoinedCount());
        assertEquals(4, joined.getCapacity());
        assertTrue(joined.isJoinedByCurrentUser());
        assertTrue(joined.isOpenForJoin());

        final EventItem notJoined = item("e-1");
        assertEquals("Gaming", notJoined.getCommunityName());
        assertEquals(0, notJoined.getJoinedCount());
        assertFalse(notJoined.isJoinedByCurrentUser());

        assertFalse(item("e-past").isOpenForJoin());
    }

    @Test
    void missingCreatorFallsBackToPlaceholderName() {
        interactor.execute(new BrowseEventsInputData("u-viewer", "study"));

        assertEquals(BrowseEventsInteractor.UNKNOWN_CREATOR, item("e-3").getCreatorDisplayName());
    }

    @Test
    void anonymousViewerHasNotJoinedAnything() {
        interactor.execute(new BrowseEventsInputData("", "ALL"));

        for (EventItem row : presenter.success.getEvents()) {
            assertFalse(row.isJoinedByCurrentUser(), row.getEventId());
        }
    }

    @Test
    void communityWithNoEventsGivesAnEmptyList() {
        interactor.execute(new BrowseEventsInputData("u-viewer", "chill"));

        assertNull(presenter.error);
        assertTrue(presenter.success.getEvents().isEmpty());
    }

    @Test
    void unknownCommunityIsRejected() {
        interactor.execute(new BrowseEventsInputData("u-viewer", "chess"));

        assertNull(presenter.success);
        assertNotNull(presenter.error);
    }

    /** Captures whatever the interactor presents. */
    private static final class RecordingPresenter implements BrowseEventsOutputBoundary {
        private BrowseEventsOutputData success;
        private String error;

        @Override
        public void prepareSuccessView(BrowseEventsOutputData outputData) {
            success = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
        }
    }
}
