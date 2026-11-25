package data_access.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import app.SqliteDataAccessBundle;
import entity.CommonUserFactory;
import entity.Community;
import entity.Event;
import entity.User;
import use_case.browse_events.BrowseEventsInputData;
import use_case.browse_events.BrowseEventsInteractor;
import use_case.browse_events.BrowseEventsOutputBoundary;
import use_case.browse_events.BrowseEventsOutputData;
import use_case.join_event.JoinEventDataAccessInterface.JoinAttemptResult;
import use_case.join_event.JoinEventDataAccessInterface.Outcome;

class SqliteEventDataAccessTest {

    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");
    private static final Instant NOW = ZonedDateTime.of(2026, 11, 24, 12, 0, 0, 0, TORONTO).toInstant();
    private static final Instant DEC_3 = ZonedDateTime.of(2026, 12, 3, 14, 0, 0, 0, TORONTO).toInstant();

    @TempDir
    Path directory;

    private String dbPath() {
        return directory.resolve("data/campusbuddy.db").toString();
    }

    private static User signUp(SqliteDataAccessBundle bundle, String userId, String username) {
        final User user = new CommonUserFactory().create(userId, username, bundle.passwordHasher().hash("secret1"));
        assertTrue(bundle.signupDataAccess().saveUser(user));
        return user;
    }

    private static Event event(String eventId, String creatorId, int capacity, Instant startsAt) {
        return new Event(eventId, Community.STUDY_ID, creatorId, "CSC207 Review", "Bring notes", capacity, startsAt);
    }

    @Test
    void firstCommunityRefreshAfterSignupListsTheDefaultCommunities() {
        final SqliteDataAccessBundle bundle = new SqliteDataAccessBundle(dbPath());
        final User user = signUp(bundle, "u-maple", "maple");
        final RecordingBrowsePresenter presenter = new RecordingBrowsePresenter();

        new BrowseEventsInteractor(bundle.browseEventsDataAccess(), presenter, Clock.fixed(NOW, TORONTO))
                .execute(new BrowseEventsInputData(user.getUserId(), BrowseEventsInputData.ALL_COMMUNITIES));

        assertNull(presenter.error);
        assertNotNull(presenter.success);
        assertEquals(Community.defaultCommunities().size(), presenter.success.getCommunities().size());
        assertTrue(presenter.success.getEvents().isEmpty());
    }

    @Test
    void publishedEventsAndJoinsSurviveReopening() {
        final SqliteDataAccessBundle first = new SqliteDataAccessBundle(dbPath());
        final User organizer = signUp(first, "u-james", "james");
        final User student = signUp(first, "u-gracie", "gracie");
        first.createEventDataAccess().saveEvent(event("e-review", organizer.getUserId(), 4, DEC_3));
        assertEquals(Outcome.JOINED,
                first.joinEventDataAccess().joinAtomically(student.getUserId(), "e-review", NOW).getOutcome());

        final SqliteDataAccessBundle reopened = new SqliteDataAccessBundle(dbPath());
        final RecordingBrowsePresenter presenter = new RecordingBrowsePresenter();
        new BrowseEventsInteractor(reopened.browseEventsDataAccess(), presenter, Clock.fixed(NOW, TORONTO))
                .execute(new BrowseEventsInputData(student.getUserId(), Community.STUDY_ID));

        assertNull(presenter.error);
        final BrowseEventsOutputData.EventItem item = presenter.success.getEvents().get(0);
        assertEquals("e-review", item.getEventId());
        assertEquals("CSC207 Review", item.getTitle());
        assertEquals(organizer.getDisplayName(), item.getCreatorDisplayName());
        assertEquals(1, item.getJoinedCount());
        assertTrue(item.isJoinedByCurrentUser());
        assertEquals(DEC_3, item.getStartsAt());

        final List<Event> december = reopened.viewCalendarDataAccess().findJoinedEventsBetween(
                student.getUserId(),
                ZonedDateTime.of(2026, 12, 1, 0, 0, 0, 0, TORONTO).toInstant(),
                ZonedDateTime.of(2027, 1, 1, 0, 0, 0, 0, TORONTO).toInstant());
        assertEquals(1, december.size());
        assertTrue(reopened.viewCalendarDataAccess().findJoinedEventsBetween(
                organizer.getUserId(), Instant.EPOCH, DEC_3.plusSeconds(1)).isEmpty());
    }

    @Test
    void joinReportsEveryOutcome() {
        final SqliteDataAccessBundle bundle = new SqliteDataAccessBundle(dbPath());
        final User organizer = signUp(bundle, "u-james", "james");
        final User gracie = signUp(bundle, "u-gracie", "gracie");
        final User henry = signUp(bundle, "u-henry", "henry");
        bundle.createEventDataAccess().saveEvent(event("e-small", organizer.getUserId(), 1, DEC_3));
        bundle.createEventDataAccess().saveEvent(event("e-past", organizer.getUserId(), 4, NOW.minusSeconds(60)));

        final JoinAttemptResult joined =
                bundle.joinEventDataAccess().joinAtomically(gracie.getUserId(), "e-small", NOW);
        assertEquals(Outcome.JOINED, joined.getOutcome());
        assertEquals(1, joined.getJoinedCount());
        assertEquals(NOW, joined.getJoinedAt());

        final JoinAttemptResult again =
                bundle.joinEventDataAccess().joinAtomically(gracie.getUserId(), "e-small", NOW.plusSeconds(5));
        assertEquals(Outcome.ALREADY_JOINED, again.getOutcome());
        assertEquals(NOW, again.getJoinedAt());

        assertEquals(Outcome.FULL,
                bundle.joinEventDataAccess().joinAtomically(henry.getUserId(), "e-small", NOW).getOutcome());
        assertEquals(Outcome.EVENT_CLOSED,
                bundle.joinEventDataAccess().joinAtomically(henry.getUserId(), "e-past", NOW).getOutcome());
        assertEquals(Outcome.EVENT_NOT_FOUND,
                bundle.joinEventDataAccess().joinAtomically(henry.getUserId(), "e-missing", NOW).getOutcome());
        assertEquals(1, bundle.browseEventsDataAccess().countParticipants("e-small"));
        assertFalse(bundle.browseEventsDataAccess().hasUserJoined(henry.getUserId(), "e-small"));
    }

    @Test
    void studentsRacingForTheLastSpotFromSeparateConnectionsGetExactlyOneSeat() throws Exception {
        final int racers = 6;
        final SqliteDataAccessBundle setup = new SqliteDataAccessBundle(dbPath());
        final User organizer = signUp(setup, "u-organizer", "organizer");
        for (int i = 0; i < racers; i++) {
            signUp(setup, "u-racer" + i, "racer" + i);
        }
        setup.createEventDataAccess().saveEvent(event("e-last-spot", organizer.getUserId(), 1, DEC_3));

        final CountDownLatch start = new CountDownLatch(1);
        final ExecutorService pool = Executors.newFixedThreadPool(racers);
        final List<Future<Outcome>> outcomes = new ArrayList<>();
        try {
            for (int i = 0; i < racers; i++) {
                final String userId = "u-racer" + i;
                // Each racer opens its own bundle, like a second app window on the same file.
                final SqliteDataAccessBundle window = new SqliteDataAccessBundle(dbPath());
                final Callable<Outcome> join = () -> {
                    start.await();
                    return window.joinEventDataAccess().joinAtomically(userId, "e-last-spot", NOW).getOutcome();
                };
                outcomes.add(pool.submit(join));
            }
            start.countDown();

            int joined = 0;
            int full = 0;
            for (Future<Outcome> outcome : outcomes) {
                final Outcome result = outcome.get(30, TimeUnit.SECONDS);
                if (result == Outcome.JOINED) {
                    joined++;
                }
                else if (result == Outcome.FULL) {
                    full++;
                }
            }
            assertEquals(1, joined);
            assertEquals(racers - 1, full);
            assertEquals(1, setup.browseEventsDataAccess().countParticipants("e-last-spot"));
        }
        finally {
            pool.shutdownNow();
        }
    }

    /** Captures whatever the browse interactor presents. */
    private static final class RecordingBrowsePresenter implements BrowseEventsOutputBoundary {
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
