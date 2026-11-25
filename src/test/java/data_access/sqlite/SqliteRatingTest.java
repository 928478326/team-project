package data_access.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import entity.BuddyRating;
import entity.CommonUserFactory;
import use_case.view_rateable_buddies.ViewRateableBuddiesOutputData.RateableBuddyItem;

class SqliteRatingTest {
    private static final Instant START = Instant.ofEpochMilli(100);

    @TempDir
    private Path directory;

    private SqliteDatabase database() {
        return new SqliteDatabase(directory.resolve("campusbuddy.db").toString());
    }

    @Test
    void upgradesAnAccountOnlyFileAndKeepsItsExistingUser() throws SQLException {
        final SqliteDatabase database = database();
        try (Connection connection = database.openConnection(); Statement sql = connection.createStatement()) {
            sql.execute("CREATE TABLE users (user_id TEXT PRIMARY KEY NOT NULL, username TEXT UNIQUE NOT NULL, "
                    + "password_hash TEXT NOT NULL, display_name TEXT NOT NULL, bio TEXT NOT NULL, "
                    + "contact_info TEXT NOT NULL, weekend_style TEXT NOT NULL, interested_communities TEXT NOT NULL)");
            sql.execute("INSERT INTO users VALUES ('henry', 'henry', 'old-hash', 'Henry', '', '', "
                    + "'STUDY_FOCUS', '[]')");
        }
        database.initializeSchema();
        try (Connection connection = database.openConnection(); Statement sql = connection.createStatement();
                ResultSet result = sql.executeQuery("SELECT username, (SELECT COUNT(*) FROM sqlite_master "
                        + "WHERE type = 'table' AND name IN ('events','participations','buddy_ratings')) FROM users")) {
            assertTrue(result.next());
            assertEquals("henry", result.getString(1));
            assertEquals(3, result.getInt(2));
        }
    }

    @Test
    void listsOnlyOtherParticipantsInSharedStartedMeetups() throws SQLException {
        final SqliteRatingDataAccess ratings = setupMeetup();
        assertTrue(ratings.findRateableBuddies("gracie", START.minusMillis(1)).isEmpty());
        assertTrue(ratings.findRateableBuddies("alex", START).isEmpty());
        final List<RateableBuddyItem> items = ratings.findRateableBuddies("gracie", START);
        assertEquals(1, items.size());
        final RateableBuddyItem item = items.get(0);
        assertEquals("henry", item.getBuddyUserId());
        assertEquals("henry", item.getBuddyDisplayName());
        assertEquals("study-1", item.getEventId());
        assertEquals("Study together", item.getEventTitle());
        assertEquals(START, item.getEventStartsAt());
        assertFalse(item.isAlreadyRated());
    }

    @Test
    void refusesFutureOrUnsharedMeetupsAndRechecksEligibilityWhenEditing() throws SQLException {
        final SqliteRatingDataAccess ratings = setupMeetup();
        assertFalse(ratings.saveIfEligible(feedback("gracie", "henry", START.minusMillis(1))));
        assertFalse(ratings.saveIfEligible(feedback("alex", "henry", START)));
        assertFalse(ratings.saveIfEligible(feedback("gracie", "alex", START)));
        assertFalse(ratings.saveIfEligible(new BuddyRating("gracie", "henry", "missing", 3, false, "", START)));
        assertTrue(ratings.saveIfEligible(feedback("gracie", "henry", START)));
        try (Connection connection = database().openConnection(); Statement sql = connection.createStatement()) {
            sql.execute("UPDATE events SET starts_at_ms = 101 WHERE event_id = 'study-1'");
        }
        assertFalse(ratings.saveIfEligible(new BuddyRating("gracie", "henry", "study-1", 5, true, "Edit", START)));
        final RateableBuddyItem previous = ratings.findRateableBuddies("gracie", START.plusMillis(1)).get(0);
        assertEquals("First", previous.getExistingComment());
    }

    @Test
    void keepsTwoMeetupsWithTheSameBuddySeparate() throws SQLException {
        final SqliteRatingDataAccess ratings = setupMeetup();
        try (Connection connection = database().openConnection(); Statement sql = connection.createStatement()) {
            sql.execute("INSERT INTO events (event_id, community_id, creator_id, title, description, capacity, "
                    + "starts_at_ms) VALUES ('study-2', 'study', 'henry', 'Another meetup', '', 3, 101)");
            sql.execute("INSERT INTO participations (user_id, event_id, joined_at_ms) VALUES "
                    + "('gracie', 'study-2', 0), ('henry', 'study-2', 0)");
        }
        assertTrue(ratings.saveIfEligible(feedback("gracie", "henry", START)));
        final Instant later = START.plusMillis(1);
        final List<RateableBuddyItem> items = ratings.findRateableBuddies("gracie", later);
        assertEquals(2, items.size());
        assertEquals("study-2", items.get(0).getEventId());
        assertFalse(items.get(0).isAlreadyRated());
        assertEquals("First", items.get(1).getExistingComment());
        assertTrue(ratings.saveIfEligible(new BuddyRating("gracie", "henry", "study-2", 5, true, "Second", later)));
        final List<RateableBuddyItem> updated = ratings.findRateableBuddies("gracie", later);
        assertEquals("Second", updated.get(0).getExistingComment());
        assertEquals("First", updated.get(1).getExistingComment());
    }

    @Test
    void reopeningShowsOnlyTheAuthorsLatestFeedback() throws SQLException {
        final SqliteRatingDataAccess ratings = setupMeetup();
        assertTrue(ratings.saveIfEligible(feedback("gracie", "henry", START)));
        assertFalse(ratings.findRateableBuddies("henry", START).get(0).isAlreadyRated());
        assertTrue(ratings.saveIfEligible(feedback("henry", "gracie", START)));
        final Instant later = START.plusSeconds(60);
        assertTrue(ratings.saveIfEligible(new BuddyRating("gracie", "henry", "study-1", 5, true, "Updated", later)));

        final SqliteDatabase reopened = database();
        reopened.initializeSchema();
        final SqliteRatingDataAccess reader = new SqliteRatingDataAccess(reopened);
        final List<RateableBuddyItem> items = reader.findRateableBuddies("gracie", later);
        assertEquals(1, items.size());
        assertTrue(items.get(0).isAlreadyRated());
        assertEquals(5, items.get(0).getExistingScore());
        assertTrue(items.get(0).isExistingWouldMeetAgain());
        assertEquals("Updated", items.get(0).getExistingComment());
        assertEquals("First", reader.findRateableBuddies("henry", later).get(0).getExistingComment());
        final List<BuddyRating> history = new SqliteRecommendationDataAccess(reopened).findAllRatings();
        assertEquals(2, history.size());
        final BuddyRating updated = history.stream().filter(rating -> "gracie".equals(rating.getRaterId()))
                .findFirst().orElseThrow();
        assertEquals("henry", updated.getRateeId());
        assertEquals("study-1", updated.getEventId());
        assertEquals(5, updated.getScore());
        assertEquals(later, updated.getRatedAt());
    }

    private BuddyRating feedback(String author, String buddy, Instant time) {
        return new BuddyRating(author, buddy, "study-1", 3, false, "First", time);
    }

    private SqliteRatingDataAccess setupMeetup() throws SQLException {
        final SqliteDatabase database = database();
        database.initializeSchema();
        insertUser(database, "gracie");
        insertUser(database, "henry");
        insertUser(database, "alex");
        try (Connection connection = database.openConnection(); Statement sql = connection.createStatement()) {
            sql.execute("INSERT INTO communities (community_id, name) VALUES ('study', 'Study')");
            sql.execute("INSERT INTO events (event_id, community_id, creator_id, title, description, "
                    + "capacity, starts_at_ms) VALUES ('study-1', 'study', 'gracie', 'Study together', '', 3, 100)");
            sql.execute("INSERT INTO participations (user_id, event_id, joined_at_ms) VALUES "
                    + "('gracie', 'study-1', 0), ('henry', 'study-1', 0)");
        }
        return new SqliteRatingDataAccess(database);
    }

    private void insertUser(SqliteDatabase database, String name) {
        assertTrue(new SqliteUserDataAccess(database).saveUser(new CommonUserFactory().create(name, name, "fixture")));
    }
}
