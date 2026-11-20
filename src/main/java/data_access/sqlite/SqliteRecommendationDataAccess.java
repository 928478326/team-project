package data_access.sqlite;

import entity.BuddyRating;
import entity.Event;
import entity.Participation;
import entity.User;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import use_case.recommend_buddies.RecommendBuddiesDataAccessInterface;

/**
 * SQLite snapshot DAO serving the 3+4 buddy recommendation use case.
 * Kept separate from {@link SqliteRatingDataAccess} and {@link SqliteUserDataAccess}
 * to respect SRP and avoid multi-developer file conflicts.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class SqliteRecommendationDataAccess
        implements RecommendBuddiesDataAccessInterface {

    private final SqliteDatabase database;

    public SqliteRecommendationDataAccess(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database, "database must not be null");
    }

    @Override
    public Optional<User> findUserById(String userId) {
        // TODO [Track A]: SELECT * FROM users WHERE user_id = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRecommendationDataAccess.findUserById");
    }

    @Override
    public List<User> findAllOtherUsers(String excludeUserId) {
        // TODO [Track A]: SELECT * FROM users WHERE user_id <> ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRecommendationDataAccess.findAllOtherUsers");
    }

    @Override
    public List<Event> findAllEvents() {
        // TODO [Track A]: SELECT * FROM events
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRecommendationDataAccess.findAllEvents");
    }

    @Override
    public List<Participation> findAllParticipations() {
        // TODO [Track A]: SELECT * FROM participations
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRecommendationDataAccess.findAllParticipations");
    }

    @Override
    public List<BuddyRating> findAllRatings() {
        // TODO [Track A]: SELECT * FROM buddy_ratings
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteRecommendationDataAccess.findAllRatings");
    }
}
