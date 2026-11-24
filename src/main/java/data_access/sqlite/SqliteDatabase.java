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
     * Initializes the account table on first launch and preserves existing accounts on restart.
     * @throws IllegalStateException if the directory or table cannot be created
     */
    public void initializeSchema() {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (Connection connection = openConnection(); Statement sql = connection.createStatement()) {
                sql.execute("CREATE TABLE IF NOT EXISTS users ("
                        + "user_id TEXT PRIMARY KEY NOT NULL, username TEXT UNIQUE NOT NULL, "
                        + "password_hash TEXT NOT NULL, display_name TEXT NOT NULL, bio TEXT NOT NULL, "
                        + "contact_info TEXT NOT NULL, weekend_style TEXT NOT NULL, "
                        + "interested_communities TEXT NOT NULL)");
            }
        }
        catch (IOException | SQLException ex) {
            throw new IllegalStateException("Could not initialize the account database.", ex);
        }
    }

    /**
     * Seeds the 5 demo student accounts and initial demo data when the {@code users} table is empty.
     *
     * @param passwordHasher password hasher for demo account passwords
     */
    public void seedDemoDataIfEmpty(PasswordHasher passwordHasher) {
        // TODO [Track A]: If SELECT COUNT(*) FROM users == 0, insert demo accounts
        // (james, herry, gracie, henry, alex), sample events, participations, enrollments, ratings.
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteDatabase.seedDemoDataIfEmpty");
    }
}
