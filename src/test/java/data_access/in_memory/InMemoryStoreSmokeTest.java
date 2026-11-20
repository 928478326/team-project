package data_access.in_memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.Community;
import entity.Event;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import use_case.join_event.JoinEventDataAccessInterface;

/**
 * Smoke tests verifying the Day-1 {@link InMemoryCampusBuddyStore} seed data and atomic join
 * semantics so Tracks B, C, and D can rely on it for independent development.
 */
class InMemoryStoreSmokeTest {

    @Test
    void testSeededStoreContainsFiveDemoAccountsAndFiveCommunities() {
        Sha256PasswordHasher hasher = new Sha256PasswordHasher();
        InMemoryCampusBuddyStore store = InMemoryCampusBuddyStore.createSeeded(hasher);
        InMemoryUserDataAccess userDao = new InMemoryUserDataAccess(store);
        InMemoryEventDataAccess eventDao = new InMemoryEventDataAccess(store);

        assertEquals(5, store.usersById().size());
        assertTrue(userDao.existsByUsername("james"));
        assertTrue(userDao.existsByUsername("herry"));
        assertTrue(userDao.existsByUsername("gracie"));
        assertTrue(userDao.existsByUsername("henry"));
        assertTrue(userDao.existsByUsername("alex"));
        assertEquals(5, eventDao.findAllCommunities().size());
    }

    @Test
    void testAtomicJoinEnforcesIdempotencyCapacityAndClosedWindow() {
        InMemoryCampusBuddyStore store = new InMemoryCampusBuddyStore();
        InMemoryEventDataAccess eventDao = new InMemoryEventDataAccess(store);
        Instant start = Instant.parse("2026-12-10T18:00:00Z");
        Event smallEvent = new Event("e-cap-1", Community.STUDY_ID, "u-host", "Mini Study", "", 1, start);
        eventDao.saveEvent(smallEvent);

        Instant beforeStart = start.minusSeconds(3600);
        JoinEventDataAccessInterface.JoinAttemptResult first =
                eventDao.joinAtomically("u-a", "e-cap-1", beforeStart);
        assertEquals(JoinEventDataAccessInterface.Outcome.JOINED, first.getOutcome());
        assertEquals(1, first.getJoinedCount());

        // Repeat join by same user is idempotent and preserves original joinedAt.
        JoinEventDataAccessInterface.JoinAttemptResult repeat =
                eventDao.joinAtomically("u-a", "e-cap-1", beforeStart.plusSeconds(60));
        assertEquals(JoinEventDataAccessInterface.Outcome.ALREADY_JOINED, repeat.getOutcome());
        assertEquals(beforeStart, repeat.getJoinedAt());
        assertEquals(1, repeat.getJoinedCount());

        // Second distinct user is rejected because capacity == 1.
        JoinEventDataAccessInterface.JoinAttemptResult full =
                eventDao.joinAtomically("u-b", "e-cap-1", beforeStart.plusSeconds(120));
        assertEquals(JoinEventDataAccessInterface.Outcome.FULL, full.getOutcome());

        // Join attempt at or after startsAt returns EVENT_CLOSED.
        Event openEvent = new Event("e-closed", Community.STUDY_ID, "u-host", "Closed", "", 5, start);
        eventDao.saveEvent(openEvent);
        JoinEventDataAccessInterface.JoinAttemptResult closed =
                eventDao.joinAtomically("u-c", "e-closed", start);
        assertEquals(JoinEventDataAccessInterface.Outcome.EVENT_CLOSED, closed.getOutcome());
    }
}
