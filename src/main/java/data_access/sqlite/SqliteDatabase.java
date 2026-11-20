package data_access.sqlite;

import entity.PasswordHasher;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * SQLite database connection factory, schema initializer (6 tables), and demo data seeder.
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

    private final String jdbcUrl;

    /**
     * Creates a database handle pointing at a file path (e.g., {@code data/campusbuddy.db}).
     *
     * @param dbFilePath path to the SQLite database file
     */
    public SqliteDatabase(String dbFilePath) {
        Objects.requireNonNull(dbFilePath, "dbFilePath must not be null");
        this.jdbcUrl = "jdbc:sqlite:" + dbFilePath;
    }

    /**
     * Opens a new SQLite JDBC connection with foreign keys and busy timeout enabled.
     *
     * @return configured JDBC connection
     * @throws SQLException if connecting fails
     */
    public Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }
        return connection;
    }

    /**
     * Creates all 6 tables ({@code users}, {@code communities}, {@code events},
     * {@code participations}, {@code course_enrollments}, {@code buddy_ratings})
     * and inserts the 5 default communities idempotently.
     */
    public void initializeSchema() {
        // TODO [Track A]: Execute CREATE TABLE IF NOT EXISTS for all 6 tables and
        // INSERT OR IGNORE the 5 default communities (study, sports, gaming, food, chill).
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteDatabase.initializeSchema");
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
