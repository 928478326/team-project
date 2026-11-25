package use_case.create_event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import data_access.in_memory.InMemoryCampusBuddyStore;
import data_access.in_memory.InMemoryEventDataAccess;
import entity.Event;

class CreateEventInteractorTest {

    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");
    /** "Now" for every test: 2026-11-24 12:00 in Toronto. */
    private static final Instant NOW = ZonedDateTime.of(2026, 11, 24, 12, 0, 0, 0, TORONTO).toInstant();

    private final InMemoryCampusBuddyStore store = new InMemoryCampusBuddyStore();
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final CreateEventInteractor interactor = new CreateEventInteractor(
            new InMemoryEventDataAccess(store), presenter, Clock.fixed(NOW, TORONTO), TORONTO);

    @Test
    void validEventIsSavedWithTorontoStartTime() {
        interactor.execute(new CreateEventInputData(
                "u-james", " STUDY ", "  CSC207 Review  ", "Bring notes", " 4 ", "2026-12-03 14:00"));

        assertNull(presenter.error);
        assertNotNull(presenter.success);
        final Event saved = store.eventsById().get(presenter.success.getEventId());
        assertNotNull(saved);
        assertEquals("study", saved.getCommunityId());
        assertEquals("CSC207 Review", saved.getTitle());
        assertEquals("u-james", saved.getCreatorId());
        assertEquals(4, saved.getCapacity());
        assertEquals(ZonedDateTime.of(2026, 12, 3, 14, 0, 0, 0, TORONTO).toInstant(), saved.getStartsAt());
        assertEquals(saved.getStartsAt(), presenter.success.getStartsAt());
        assertEquals("CSC207 Review", presenter.success.getTitle());
    }

    @Test
    void creatorIsNotAutomaticallyJoined() {
        interactor.execute(new CreateEventInputData(
                "u-james", "food", "Hotpot", "", "5", "2026-12-10 18:30"));

        assertNotNull(presenter.success);
        assertTrue(store.participationsByKey().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"1", "100"})
    void capacityBoundariesAreAccepted(String capacity) {
        interactor.execute(new CreateEventInputData(
                "u-james", "sports", "Pickup basketball", "", capacity, "2026-12-01 09:00"));

        assertNull(presenter.error);
        assertEquals(Integer.parseInt(capacity), presenter.success.getCapacity());
    }

    @ParameterizedTest(name = "[{index}] {5}")
    @CsvSource({
        "'',      study, Review, 4,   2026-12-03 14:00, missing creator",
        "u-james, '',    Review, 4,   2026-12-03 14:00, missing community",
        "u-james, chess, Review, 4,   2026-12-03 14:00, unknown community",
        "u-james, study, '   ',  4,   2026-12-03 14:00, blank title",
        "u-james, study, Review, abc, 2026-12-03 14:00, non-numeric capacity",
        "u-james, study, Review, 2.5, 2026-12-03 14:00, decimal capacity",
        "u-james, study, Review, 0,   2026-12-03 14:00, capacity below minimum",
        "u-james, study, Review, 101, 2026-12-03 14:00, capacity above maximum",
        "u-james, study, Review, 4,   2026/12/03 14:00, wrong date format",
        "u-james, study, Review, 4,   2026-02-30 14:00, impossible date",
        "u-james, study, Review, 4,   2026-12-03 25:00, impossible hour",
        "u-james, study, Review, 4,   '',               missing start time",
        "u-james, study, Review, 4,   2026-11-24 11:59, start time in the past",
        "u-james, study, Review, 4,   2026-11-24 12:00, start time exactly now"
    })
    void invalidInputIsRejectedAndNothingIsSaved(
            String creator, String community, String title, String capacity, String startsAt, String reason) {
        interactor.execute(new CreateEventInputData(creator, community, title, "", capacity, startsAt));

        assertNull(presenter.success, reason);
        assertNotNull(presenter.error, reason);
        assertFalse(presenter.error.isBlank(), reason);
        assertTrue(store.eventsById().isEmpty(), reason);
    }

    @Test
    void saveFailureIsReportedToTheUser() {
        final CreateEventDataAccessInterface failingDao = new CreateEventDataAccessInterface() {
            @Override
            public boolean communityExists(String communityId) {
                return true;
            }

            @Override
            public void saveEvent(Event event) {
                throw new IllegalStateException("database is locked");
            }
        };
        final CreateEventInteractor failing =
                new CreateEventInteractor(failingDao, presenter, Clock.fixed(NOW, TORONTO), TORONTO);

        failing.execute(new CreateEventInputData("u-james", "study", "Review", "", "4", "2026-12-03 14:00"));

        assertNull(presenter.success);
        assertNotNull(presenter.error);
    }

    @ParameterizedTest
    @CsvSource({"2027-03-14 02:00", "2027-03-14 02:30", "2027-03-14 02:59"})
    void startTimeSkippedByDaylightSavingIsRejected(String startsAt) {
        interactor.execute(new CreateEventInputData("u-james", "study", "Review", "", "4", startsAt));

        assertNull(presenter.success);
        assertNotNull(presenter.error);
        assertTrue(presenter.error.contains("daylight saving"), presenter.error);
        assertTrue(store.eventsById().isEmpty());
    }

    @Test
    void timesAroundTheSpringForwardGapAreAccepted() {
        interactor.execute(new CreateEventInputData(
                "u-james", "study", "Before", "", "4", "2027-03-14 01:59"));
        assertNotNull(presenter.success);
        assertEquals(ZonedDateTime.of(2027, 3, 14, 1, 59, 0, 0, TORONTO).toInstant(),
                presenter.success.getStartsAt());

        interactor.execute(new CreateEventInputData(
                "u-james", "study", "After", "", "4", "2027-03-14 03:00"));
        assertEquals(ZonedDateTime.of(2027, 3, 14, 3, 0, 0, 0, TORONTO).toInstant(),
                presenter.success.getStartsAt());
        assertEquals(2, store.eventsById().size());
    }

    @Test
    void repeatedFallBackHourUsesTheFirstOccurrence() {
        interactor.execute(new CreateEventInputData(
                "u-james", "study", "Late review", "", "4", "2027-11-07 01:30"));

        assertNull(presenter.error);
        // 01:30 happens twice that night; the first one is still on daylight time (UTC-4).
        assertEquals(Instant.parse("2027-11-07T05:30:00Z"), presenter.success.getStartsAt());
    }

    @Test
    void storageFailureWhileCheckingTheCommunityIsReportedToTheUser() {
        final CreateEventDataAccessInterface failingDao = new CreateEventDataAccessInterface() {
            @Override
            public boolean communityExists(String communityId) {
                throw new IllegalStateException("database is locked");
            }

            @Override
            public void saveEvent(Event event) {
                throw new AssertionError("must not save when the community check failed");
            }
        };
        final CreateEventInteractor failing =
                new CreateEventInteractor(failingDao, presenter, Clock.fixed(NOW, TORONTO), TORONTO);

        failing.execute(new CreateEventInputData("u-james", "study", "Review", "", "4", "2026-12-03 14:00"));

        assertNull(presenter.success);
        assertNotNull(presenter.error);
    }

    /** Captures whatever the interactor presents. */
    private static final class RecordingPresenter implements CreateEventOutputBoundary {
        private CreateEventOutputData success;
        private String error;

        @Override
        public void prepareSuccessView(CreateEventOutputData outputData) {
            success = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
        }
    }
}
