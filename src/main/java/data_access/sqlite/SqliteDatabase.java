package data_access.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import entity.PasswordHasher;

/**
 * SQLite connection factory and schema initializer for CampusBuddy.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 *
 * <p>Every opened JDBC connection MUST enable:
 * <pre>
 *   PRAGMA foreign_keys = ON;
 *   PRAGMA busy_timeout = 5000;
 * </pre>
 */
public final class SqliteDatabase {

    private final Path file;

    /**
     * Creates a database handle pointing at a file path (e.g., {@code data/campusbuddy.db}).
     *
     * @param dbFilePath path to the SQLite database file
     */
    public SqliteDatabase(String dbFilePath) {
        Objects.requireNonNull(dbFilePath, "dbFilePath must not be null");
        this.file = Path.of(dbFilePath);
    }

    /**
     * Opens a new SQLite JDBC connection with foreign keys and busy timeout enabled.
     *
     * @return configured JDBC connection
     * @throws SQLException if connecting fails
     */
    public Connection openConnection() throws SQLException {
        final Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }
        catch (SQLException ex) {
            connection.close();
            throw ex;
        }
        return connection;
    }

    /**
     * Creates missing account, event, participation, and rating tables on startup.
     * @throws IllegalStateException if the directory or schema cannot be created
     */
    public void initializeSchema() {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (Connection connection = openConnection(); Statement sql = connection.createStatement()) {
                connection.setAutoCommit(false);
                sql.execute("CREATE TABLE IF NOT EXISTS users ("
                        + "user_id TEXT PRIMARY KEY NOT NULL, username TEXT UNIQUE NOT NULL, "
                        + "password_hash TEXT NOT NULL, display_name TEXT NOT NULL, bio TEXT NOT NULL, "
                        + "contact_info TEXT NOT NULL, weekend_style TEXT NOT NULL, "
                        + "interested_communities TEXT NOT NULL)");
                createMeetupTables(sql);
                connection.commit();
            }
        }
        catch (IOException | SQLException ex) {
            throw new IllegalStateException("Could not initialize the CampusBuddy database.", ex);
        }
    }

    private void createMeetupTables(Statement sql) throws SQLException {
        sql.execute("CREATE TABLE IF NOT EXISTS communities (community_id TEXT PRIMARY KEY NOT NULL, "
                + "name TEXT NOT NULL)");
        sql.execute("CREATE TABLE IF NOT EXISTS events (event_id TEXT PRIMARY KEY NOT NULL, "
                + "community_id TEXT NOT NULL REFERENCES communities(community_id), "
                + "creator_id TEXT NOT NULL REFERENCES users(user_id), title TEXT NOT NULL, "
                + "description TEXT NOT NULL, capacity INTEGER NOT NULL CHECK (capacity BETWEEN 1 AND 100), "
                + "starts_at_ms INTEGER NOT NULL)");
        sql.execute("CREATE TABLE IF NOT EXISTS participations (user_id TEXT NOT NULL REFERENCES users(user_id), "
                + "event_id TEXT NOT NULL REFERENCES events(event_id), joined_at_ms INTEGER NOT NULL, "
                + "PRIMARY KEY (user_id, event_id))");
        sql.execute("CREATE INDEX IF NOT EXISTS participants_by_event ON participations (event_id, user_id)");
        sql.execute("CREATE TABLE IF NOT EXISTS buddy_ratings (rater_id TEXT NOT NULL, ratee_id TEXT NOT NULL, "
                + "event_id TEXT NOT NULL, score INTEGER NOT NULL CHECK (score BETWEEN 1 AND 5), "
                + "would_meet_again INTEGER NOT NULL CHECK (would_meet_again IN (0, 1)), comment TEXT NOT NULL, "
                + "rated_at_ms INTEGER NOT NULL, PRIMARY KEY (rater_id, ratee_id, event_id), "
                + "CHECK (rater_id <> ratee_id), "
                + "FOREIGN KEY (rater_id, event_id) REFERENCES participations(user_id, event_id), "
                + "FOREIGN KEY (ratee_id, event_id) REFERENCES participations(user_id, event_id))");
    }

    /**
     * Seeds the 5 demo student accounts and initial demo data when the {@code users} table is empty.
     *
     * @param passwordHasher password hasher for demo account passwords
     * @throws UnsupportedOperationException until the demo fixtures are implemented
     */
    public void seedDemoDataIfEmpty(PasswordHasher passwordHasher) {
        // TODO [Track A]: If SELECT COUNT(*) FROM users == 0, insert demo accounts
        // (james, herry, gracie, henry, alex), sample events, participations, enrollments, ratings.
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteDatabase.seedDemoDataIfEmpty");
    }
}
