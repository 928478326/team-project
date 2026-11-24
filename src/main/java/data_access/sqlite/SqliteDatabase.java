package data_access.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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
     * Initializes the account table on first launch and migrates pre-existing schemas idempotently
     * so existing accounts retain safe defaults across restarts.
     *
     * @throws IllegalStateException if the directory or table cannot be created or migrated
     */
    public void initializeSchema() {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (Connection connection = openConnection();
                    Statement sql = connection.createStatement()) {
                sql.execute("CREATE TABLE IF NOT EXISTS users ("
                        + "user_id TEXT PRIMARY KEY NOT NULL, username TEXT UNIQUE NOT NULL, "
                        + "password_hash TEXT NOT NULL, display_name TEXT NOT NULL, bio TEXT NOT NULL, "
                        + "contact_info TEXT NOT NULL, weekend_style TEXT NOT NULL, "
                        + "interested_communities TEXT NOT NULL, "
                        + "utorid TEXT NOT NULL DEFAULT '', "
                        + "share_contact_info INTEGER NOT NULL DEFAULT 1, "
                        + "uoft_verified INTEGER NOT NULL DEFAULT 0, "
                        + "llm_persona TEXT NOT NULL DEFAULT '')");
                migrateUsersColumns(sql);
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
     * @throws UnsupportedOperationException until the demo fixtures are implemented
     */
    public void seedDemoDataIfEmpty(PasswordHasher passwordHasher) {
        // TODO [Track A]: If SELECT COUNT(*) FROM users == 0, insert demo accounts
        // (james, herry, gracie, henry, alex), sample events, participations, enrollments, ratings.
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteDatabase.seedDemoDataIfEmpty");
    }

    private static void migrateUsersColumns(Statement sql) throws SQLException {
        final Set<String> columns = new LinkedHashSet<>();
        try (ResultSet info = sql.executeQuery("PRAGMA table_info(users)")) {
            while (info.next()) {
                columns.add(info.getString("name"));
            }
        }
        addColumnIfMissing(sql, columns, "utorid", "TEXT NOT NULL DEFAULT ''");
        addColumnIfMissing(sql, columns, "share_contact_info", "INTEGER NOT NULL DEFAULT 1");
        addColumnIfMissing(sql, columns, "uoft_verified", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing(sql, columns, "llm_persona", "TEXT NOT NULL DEFAULT ''");
    }

    private static void addColumnIfMissing(
            Statement sql, Set<String> existingColumns, String columnName, String definition)
            throws SQLException {
        if (!existingColumns.contains(columnName)) {
            sql.execute("ALTER TABLE users ADD COLUMN " + columnName + " " + definition);
        }
    }
}
