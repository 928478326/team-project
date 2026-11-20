package data_access.sqlite;

import entity.Community;
import entity.Event;
import entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import use_case.browse_events.BrowseEventsDataAccessInterface;
import use_case.create_event.CreateEventDataAccessInterface;
import use_case.join_event.JoinEventDataAccessInterface;
import use_case.view_calendar.ViewCalendarDataAccessInterface;

/**
 * SQLite implementation of community event, atomic participation, and calendar DAIs.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class SqliteEventDataAccess
        implements BrowseEventsDataAccessInterface,
                CreateEventDataAccessInterface,
                JoinEventDataAccessInterface,
                ViewCalendarDataAccessInterface {

    private final SqliteDatabase database;

    public SqliteEventDataAccess(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database, "database must not be null");
    }

    @Override
    public boolean communityExists(String communityId) {
        // TODO [Track A]: SELECT 1 FROM communities WHERE community_id = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteEventDataAccess.communityExists");
    }

    @Override
    public List<Community> findAllCommunities() {
        // TODO [Track A]: SELECT community_id, name FROM communities ORDER BY community_id
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteEventDataAccess.findAllCommunities");
    }

    @Override
    public List<Event> findEventsByCommunity(String communityId) {
        // TODO [Track A]: SELECT * FROM events (filtered when communityId != ALL) ORDER BY starts_at_ms, event_id
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteEventDataAccess.findEventsByCommunity");
    }

    @Override
    public int countParticipants(String eventId) {
        // TODO [Track A]: SELECT COUNT(*) FROM participations WHERE event_id = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteEventDataAccess.countParticipants");
    }

    @Override
    public boolean hasUserJoined(String userId, String eventId) {
        // TODO [Track A]: SELECT 1 FROM participations WHERE user_id = ? AND event_id = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteEventDataAccess.hasUserJoined");
    }

    @Override
    public Optional<User> findUserById(String userId) {
        // TODO [Track A]: Query users table by user_id
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteEventDataAccess.findUserById");
    }

    @Override
    public void saveEvent(Event event) {
        // TODO [Track A]: INSERT INTO events (...) VALUES (...)
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteEventDataAccess.saveEvent");
    }

    @Override
    public Optional<Event> findEventById(String eventId) {
        // TODO [Track A]: SELECT * FROM events WHERE event_id = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteEventDataAccess.findEventById");
    }

    @Override
    public JoinAttemptResult joinAtomically(String userId, String eventId, Instant now) {
        // TODO [Track A]: Execute BEGIN IMMEDIATE transaction:
        // 1. Check event existence (EVENT_NOT_FOUND)
        // 2. Check existing participation (ALREADY_JOINED preserving original joined_at_ms)
        // 3. Check now < starts_at_ms (EVENT_CLOSED)
        // 4. Check currentCount < capacity (FULL)
        // 5. INSERT INTO participations and COMMIT (JOINED)
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteEventDataAccess.joinAtomically");
    }

    @Override
    public List<Event> findJoinedEventsBetween(
            String userId, Instant inclusiveStart, Instant exclusiveEnd) {
        // TODO [Track A]: JOIN events & participations WHERE user_id=? AND starts_at_ms >= ? AND starts_at_ms < ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteEventDataAccess.findJoinedEventsBetween");
    }
}
