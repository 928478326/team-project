package use_case.join_event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import data_access.in_memory.InMemoryCampusBuddyStore;
import data_access.in_memory.InMemoryEventDataAccess;
import entity.Community;
import entity.Event;

class JoinEventInteractorTest {

    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");
    private static final Instant NOW = ZonedDateTime.of(2026, 11, 24, 12, 0, 0, 0, TORONTO).toInstant();
    private static final Instant STARTS_AT = ZonedDateTime.of(2026, 12, 3, 14, 0, 0, 0, TORONTO).toInstant();
    private static final String EVENT_ID = "e-review";

    private final InMemoryCampusBuddyStore store = new InMemoryCampusBuddyStore();
    private final InMemoryEventDataAccess dataAccess = new InMemoryEventDataAccess(store);
    private final RecordingPresenter presenter = new RecordingPresenter();

    JoinEventInteractorTest() {
        store.putEvent(new Event(EVENT_ID, Community.STUDY_ID, "u-james", "CSC207 Review", "", 2, STARTS_AT));
    }

    private JoinEventInteractor interactorAt(Instant now) {
        return new JoinEventInteractor(dataAccess, presenter, Clock.fixed(now, TORONTO));
    }

    @Test
    void joiningAnOpenEventAddsTheStudent() {
        interactorAt(NOW).execute(new JoinEventInputData("u-gracie", EVENT_ID));

        assertNull(presenter.error);
        assertEquals(EVENT_ID, presenter.success.getEventId());
        assertFalse(presenter.success.isAlreadyJoined());
        assertEquals(1, presenter.success.getJoinedCount());
        assertEquals(2, presenter.success.getCapacity());
        assertTrue(dataAccess.hasUserJoined("u-gracie", EVENT_ID));
    }

    @Test
    void joiningTwiceIsASuccessWithoutAddingASecondSeat() {
        final JoinEventInteractor interactor = interactorAt(NOW);
        interactor.execute(new JoinEventInputData("u-gracie", EVENT_ID));
        interactor.execute(new JoinEventInputData("u-gracie", EVENT_ID));

        assertNull(presenter.error);
        assertTrue(presenter.success.isAlreadyJoined());
        assertEquals(1, presenter.success.getJoinedCount());
        assertEquals(1, dataAccess.countParticipants(EVENT_ID));
    }

    @Test
    void aFullEventRejectsANewStudent() {
        final JoinEventInteractor interactor = interactorAt(NOW);
        interactor.execute(new JoinEventInputData("u-gracie", EVENT_ID));
        interactor.execute(new JoinEventInputData("u-henry", EVENT_ID));
        presenter.reset();

        interactor.execute(new JoinEventInputData("u-alex", EVENT_ID));

        assertNull(presenter.success);
        assertTrue(presenter.error.toLowerCase().contains("full"));
        assertFalse(dataAccess.hasUserJoined("u-alex", EVENT_ID));
        assertEquals(2, dataAccess.countParticipants(EVENT_ID));
    }

    @Test
    void aFullEventStillTreatsAnExistingMemberAsJoined() {
        final JoinEventInteractor interactor = interactorAt(NOW);
        interactor.execute(new JoinEventInputData("u-gracie", EVENT_ID));
        interactor.execute(new JoinEventInputData("u-henry", EVENT_ID));

        interactor.execute(new JoinEventInputData("u-gracie", EVENT_ID));

        assertNull(presenter.error);
        assertTrue(presenter.success.isAlreadyJoined());
    }

    @Test
    void joiningClosesExactlyWhenTheEventStarts() {
        interactorAt(STARTS_AT).execute(new JoinEventInputData("u-gracie", EVENT_ID));

        assertNull(presenter.success);
        assertNotNull(presenter.error);
        assertFalse(dataAccess.hasUserJoined("u-gracie", EVENT_ID));
    }

    @Test
    void joiningOneMinuteBeforeTheStartIsAllowed() {
        interactorAt(STARTS_AT.minusSeconds(60)).execute(new JoinEventInputData("u-gracie", EVENT_ID));

        assertNull(presenter.error);
        assertNotNull(presenter.success);
    }

    @Test
    void unknownEventIsRejected() {
        interactorAt(NOW).execute(new JoinEventInputData("u-gracie", "e-missing"));

        assertNull(presenter.success);
        assertNotNull(presenter.error);
    }

    @ParameterizedTest
    @CsvSource({"'', e-review", "u-gracie, ''", "'   ', e-review"})
    void missingIdsAreRejectedBeforeTouchingStorage(String userId, String eventId) {
        interactorAt(NOW).execute(new JoinEventInputData(userId, eventId));

        assertNull(presenter.success);
        assertNotNull(presenter.error);
        assertTrue(store.participationsByKey().isEmpty());
    }

    @Test
    void storageFailureIsReportedToTheUser() {
        final JoinEventDataAccessInterface failingDao = new JoinEventDataAccessInterface() {
            @Override
            public Optional<Event> findEventById(String eventId) {
                return Optional.empty();
            }

            @Override
            public JoinAttemptResult joinAtomically(String userId, String eventId, Instant now) {
                throw new IllegalStateException("database is locked");
            }
        };

        new JoinEventInteractor(failingDao, presenter, Clock.fixed(NOW, TORONTO))
                .execute(new JoinEventInputData("u-gracie", EVENT_ID));

        assertNull(presenter.success);
        assertNotNull(presenter.error);
    }

    /** Captures whatever the interactor presents last. */
    private static final class RecordingPresenter implements JoinEventOutputBoundary {
        private JoinEventOutputData success;
        private String error;

        @Override
        public void prepareSuccessView(JoinEventOutputData outputData) {
            success = outputData;
            error = null;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
            success = null;
        }

        void reset() {
            success = null;
            error = null;
        }
    }
}
