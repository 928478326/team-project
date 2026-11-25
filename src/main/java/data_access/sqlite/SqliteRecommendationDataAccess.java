package data_access.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import entity.BuddyRating;
import entity.Event;
import entity.Participation;
import entity.User;
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
        throw new UnsupportedOperationException(
                "TODO [Track A]: Implement SqliteRecommendationDataAccess.findUserById");
    }

    @Override
    public List<User> findAllOtherUsers(String excludeUserId) {
        // TODO [Track A]: SELECT * FROM users WHERE user_id <> ?
        throw new UnsupportedOperationException(
                "TODO [Track A]: Implement SqliteRecommendationDataAccess.findAllOtherUsers");
    }

    @Override
    public List<Event> findAllEvents() {
        // TODO [Track A]: SELECT * FROM events
        throw new UnsupportedOperationException(
                "TODO [Track A]: Implement SqliteRecommendationDataAccess.findAllEvents");
    }

    @Override
    public List<Participation> findAllParticipations() {
        // TODO [Track A]: SELECT * FROM participations
        throw new UnsupportedOperationException(
                "TODO [Track A]: Implement SqliteRecommendationDataAccess.findAllParticipations");
    }

    @Override
    public List<BuddyRating> findAllRatings() {
        final String query = "SELECT rater_id, ratee_id, event_id, score, would_meet_again, comment, rated_at_ms "
                + "FROM buddy_ratings ORDER BY rated_at_ms, rater_id, ratee_id, event_id";
        try (Connection connection = database.openConnection(); Statement sql = connection.createStatement();
                ResultSet rows = sql.executeQuery(query)) {
            final List<BuddyRating> ratings = new ArrayList<>();
            while (rows.next()) {
                ratings.add(new BuddyRating(rows.getString("rater_id"), rows.getString("ratee_id"),
                        rows.getString("event_id"), rows.getInt("score"), rows.getBoolean("would_meet_again"),
                        rows.getString("comment"), Instant.ofEpochMilli(rows.getLong("rated_at_ms"))));
            }
            return ratings;
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not load rating history.", ex);
        }
    }
}
