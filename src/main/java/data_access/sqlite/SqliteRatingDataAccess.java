package data_access.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.BuddyRating;
import use_case.rate_buddy.RateBuddyDataAccessInterface;
import use_case.view_rateable_buddies.ViewRateableBuddiesDataAccessInterface;
import use_case.view_rateable_buddies.ViewRateableBuddiesOutputData.RateableBuddyItem;

/** Reads shared meetups and stores feedback for eligible participants. */
public final class SqliteRatingDataAccess
        implements ViewRateableBuddiesDataAccessInterface, RateBuddyDataAccessInterface {
    private static final String SHARED_MEETUPS = " FROM events e "
            + "JOIN participations author ON author.event_id = e.event_id "
            + "JOIN participations buddy ON buddy.event_id = e.event_id AND buddy.user_id <> author.user_id ";
    private static final String ELIGIBLE = " WHERE author.user_id = ? AND e.starts_at_ms <= ? ";
    private final SqliteDatabase database;

    public SqliteRatingDataAccess(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database);
    }

    @Override
    public List<RateableBuddyItem> findRateableBuddies(String userId, Instant asOf) {
        final String query = "SELECT e.event_id, e.title, e.starts_at_ms, buddy.user_id, u.display_name, "
                + "r.score, r.would_meet_again, r.comment" + SHARED_MEETUPS
                + "JOIN users u ON u.user_id = buddy.user_id "
                + "LEFT JOIN buddy_ratings r ON r.event_id = e.event_id "
                + "AND r.rater_id = author.user_id AND r.ratee_id = buddy.user_id" + ELIGIBLE
                + "ORDER BY e.starts_at_ms DESC, e.event_id, buddy.user_id";
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(query)) {
            sql.setString(1, userId);
            sql.setLong(2, asOf.toEpochMilli());
            final List<RateableBuddyItem> items = new ArrayList<>();
            try (ResultSet rows = sql.executeQuery()) {
                while (rows.next()) {
                    items.add(readItem(rows));
                }
            }
            return items;
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not read shared meetups.", ex);
        }
    }

    private RateableBuddyItem readItem(ResultSet row) throws SQLException {
        final int score = row.getInt("score");
        final boolean alreadyRated = !row.wasNull();
        return new RateableBuddyItem(row.getString("event_id"), row.getString("title"),
                Instant.ofEpochMilli(row.getLong("starts_at_ms")), row.getString("user_id"),
                row.getString("display_name"), alreadyRated, score, row.getBoolean("would_meet_again"),
                row.getString("comment"));
    }

    @Override
    public boolean saveIfEligible(BuddyRating rating) {
        final String update = "INSERT INTO buddy_ratings "
                + "(rater_id, ratee_id, event_id, score, would_meet_again, comment, rated_at_ms) "
                + "SELECT author.user_id, buddy.user_id, e.event_id, ?, ?, ?, ?" + SHARED_MEETUPS + ELIGIBLE
                + "AND buddy.user_id = ? AND e.event_id = ? "
                + "ON CONFLICT(rater_id, ratee_id, event_id) DO UPDATE SET score = excluded.score, "
                + "would_meet_again = excluded.would_meet_again, comment = excluded.comment, "
                + "rated_at_ms = excluded.rated_at_ms";
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(update)) {
            final long ratedAt = rating.getRatedAt().toEpochMilli();
            bind(sql, rating.getScore(), rating.isWouldMeetAgain(), rating.getComment(), ratedAt,
                    rating.getRaterId(), ratedAt, rating.getRateeId(), rating.getEventId());
            return sql.executeUpdate() == 1;
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not save buddy rating.", ex);
        }
    }

    private void bind(PreparedStatement sql, Object... values) throws SQLException {
        int parameter = 1;
        for (Object value : values) {
            sql.setObject(parameter, value);
            parameter++;
        }
    }
}
