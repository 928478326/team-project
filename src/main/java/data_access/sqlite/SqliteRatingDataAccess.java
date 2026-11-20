package data_access.sqlite;

import entity.BuddyRating;
import entity.Event;
import entity.Participation;
import entity.User;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import use_case.rate_buddy.RateBuddyDataAccessInterface;
import use_case.view_rateable_buddies.ViewRateableBuddiesDataAccessInterface;

/**
 * SQLite implementation of buddy rating Data Access Interfaces.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class SqliteRatingDataAccess
        implements ViewRateableBuddiesDataAccessInterface, RateBuddyDataAccessInterface {

    private final SqliteDatabase database;

    public SqliteRatingDataAccess(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database, "database must not be null");
    }

    @Override
    public List<Event> findEventsJoinedByUser(String userId) {
        // TODO [Track A]: SELECT e.* FROM events e JOIN participations p ON e.event_id = p.event_id WHERE p.user_id = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRatingDataAccess.findEventsJoinedByUser");
    }

    @Override
    public List<Participation> findParticipationsByEventId(String eventId) {
        // TODO [Track A]: SELECT * FROM participations WHERE event_id = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRatingDataAccess.findParticipationsByEventId");
    }

    @Override
    public Map<String, User> findUsersByIds(Set<String> userIds) {
        // TODO [Track A]: SELECT * FROM users WHERE user_id IN (...)
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRatingDataAccess.findUsersByIds");
    }

    @Override
    public List<BuddyRating> findRatingsByRater(String raterId) {
        // TODO [Track A]: SELECT * FROM buddy_ratings WHERE rater_id = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRatingDataAccess.findRatingsByRater");
    }

    @Override
    public boolean haveUsersSharedEvent(String raterId, String rateeId, String eventId) {
        // TODO [Track A]: Verify both raterId and rateeId exist in participations for eventId
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRatingDataAccess.haveUsersSharedEvent");
    }

    @Override
    public void upsertRating(BuddyRating rating) {
        // TODO [Track A]: INSERT INTO buddy_ratings (...) VALUES (...)
        // ON CONFLICT(rater_id, ratee_id, event_id) DO UPDATE SET score=..., would_meet_again=..., comment=..., rated_at_ms=...
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRatingDataAccess.upsertRating");
    }
}
