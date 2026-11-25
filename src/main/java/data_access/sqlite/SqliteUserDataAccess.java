package data_access.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;

import entity.LlmPersona;
import entity.PrivateProfile;
import entity.PublicProfile;
import entity.User;
import entity.WeekendStyle;
import use_case.edit_profile.EditProfileDataAccessInterface;
import use_case.login.LoginDataAccessInterface;
import use_case.signup.SignupDataAccessInterface;

/** Maps the account and profile use cases to the same SQLite user record. */
public final class SqliteUserDataAccess
        implements SignupDataAccessInterface, LoginDataAccessInterface, EditProfileDataAccessInterface {

    private final SqliteDatabase database;

    /**
     * Constructs a SQLite-backed user DAO.
     *
     * @param database initialized SQLite database handle (non-null)
     */
    public SqliteUserDataAccess(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database);
    }

    @Override
    public boolean saveUser(User user) {
        final String insert = "INSERT INTO users (user_id, username, password_hash, display_name, bio, "
                + "contact_info, weekend_style, interested_communities, utorid, share_contact_info, "
                + "uoft_verified, llm_persona) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(username) DO NOTHING";
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(insert)) {
            final int profileStart = bindStrings(
                    sql, 1, user.getUserId(), user.getUsername(), user.getPasswordHash());
            bindProfile(sql, profileStart, user);
            return sql.executeUpdate() == 1;
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not create account.", ex);
        }
    }

    @Override
    public Optional<User> findByUsername(String normalizedUsername) {
        return find("SELECT * FROM users WHERE username = ?", normalizedUsername);
    }

    @Override
    public Optional<User> findUserById(String userId) {
        return find("SELECT * FROM users WHERE user_id = ?", userId);
    }

    private Optional<User> find(String query, String value) {
        try (Connection connection = database.openConnection()) {
            return find(connection, query, value);
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not read account.", ex);
        }
    }

    private Optional<User> find(Connection connection, String query, String value) throws SQLException {
        try (PreparedStatement sql = connection.prepareStatement(query)) {
            sql.setString(1, value);
            try (ResultSet row = sql.executeQuery()) {
                Optional<User> found = Optional.empty();
                if (row.next()) {
                    found = Optional.of(readUser(row));
                }
                return found;
            }
        }
    }

    private User readUser(ResultSet row) throws SQLException {
        final JSONArray interests = new JSONArray(row.getString("interested_communities"));
        final Set<String> ids = new LinkedHashSet<>();
        for (int index = 0; index < interests.length(); index++) {
            ids.add(interests.getString(index));
        }
        final PublicProfile publicProfile = new PublicProfile(
                row.getString("display_name"),
                row.getString("bio"),
                row.getString("contact_info"),
                WeekendStyle.fromStored(row.getString("weekend_style")),
                ids);
        final PrivateProfile privateProfile = new PrivateProfile(
                row.getString("utorid"),
                row.getInt("share_contact_info") != 0,
                row.getInt("uoft_verified") != 0);
        final LlmPersona llmPersona = LlmPersona.fromStored(row.getString("llm_persona"));
        return new User(
                row.getString("user_id"),
                row.getString("username"),
                row.getString("password_hash"),
                publicProfile,
                privateProfile,
                llmPersona);
    }

    @Override
    public void updateUserProfile(User user) {
        try (Connection connection = database.openConnection()) {
            writeProfile(connection, user);
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not update profile.", ex);
        }
    }

    @Override
    public boolean updateIfUnchanged(User expected, User updated) {
        try (Connection connection = database.openConnection(); Statement transaction = connection.createStatement()) {
            transaction.execute("BEGIN IMMEDIATE");
            boolean committed = false;
            try {
                final Optional<User> current = find(connection, "SELECT * FROM users WHERE user_id = ?",
                        updated.getUserId());
                final boolean matches = current.isPresent() && current.get().hasSameProfileAs(expected);
                if (matches) {
                    writeProfile(connection, updated);
                }
                transaction.execute("COMMIT");
                committed = true;
                return matches;
            }
            finally {
                if (!committed) {
                    transaction.execute("ROLLBACK");
                }
            }
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not update profile.", ex);
        }
    }

    private void writeProfile(Connection connection, User user) throws SQLException {
        final String update = "UPDATE users SET display_name=?, bio=?, contact_info=?, weekend_style=?, "
                + "interested_communities=?, utorid=?, share_contact_info=?, uoft_verified=?, "
                + "llm_persona=? WHERE user_id=?";
        try (PreparedStatement sql = connection.prepareStatement(update)) {
            final int idParameter = bindProfile(sql, 1, user);
            sql.setString(idParameter, user.getUserId());
            if (sql.executeUpdate() == 0) {
                throw new IllegalArgumentException("Unknown user ID.");
            }
        }
    }

    private int bindProfile(PreparedStatement sql, int first, User user) throws SQLException {
        final PrivateProfile privateProfile = user.getPrivateProfile();
        int cursor = bindStrings(
                sql,
                first,
                user.getDisplayName(),
                user.getBio(),
                user.getContactInfo(),
                user.getWeekendStyle().name(),
                new JSONArray(user.getInterestedCommunityIds()).toString(),
                privateProfile.getUtorid());
        sql.setInt(cursor, booleanToInt(privateProfile.isShareContactInfo()));
        cursor++;
        sql.setInt(cursor, booleanToInt(privateProfile.isUoftVerified()));
        cursor++;
        sql.setString(cursor, user.getLlmPersona().getSummary());
        cursor++;
        return cursor;
    }

    private int bindStrings(PreparedStatement sql, int first, String... values) throws SQLException {
        int parameter = first;
        for (String value : values) {
            sql.setString(parameter, value);
            parameter++;
        }
        return parameter;
    }

    private static int booleanToInt(boolean flag) {
        final int result;
        if (flag) {
            result = 1;
        }
        else {
            result = 0;
        }
        return result;
    }
}
