package data_access.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;

import entity.User;
import entity.WeekendStyle;
import use_case.edit_profile.EditProfileDataAccessInterface;
import use_case.login.LoginDataAccessInterface;
import use_case.signup.SignupDataAccessInterface;

/** Maps the account and profile use cases to the same SQLite user record. */
public final class SqliteUserDataAccess
        implements SignupDataAccessInterface, LoginDataAccessInterface, EditProfileDataAccessInterface {
    private final SqliteDatabase database;

    public SqliteUserDataAccess(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database);
    }

    @Override
    public boolean saveUser(User user) {
        final String insert = "INSERT INTO users (user_id, username, password_hash, display_name, bio, "
                + "contact_info, weekend_style, interested_communities) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(username) DO NOTHING";
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(insert)) {
            final int profileStart = bindStrings(sql, 1, user.getUserId(), user.getUsername(), user.getPasswordHash());
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
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(query)) {
            sql.setString(1, value);
            try (ResultSet row = sql.executeQuery()) {
                Optional<User> found = Optional.empty();
                if (row.next()) {
                    found = Optional.of(readUser(row));
                }
                return found;
            }
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not read account.", ex);
        }
    }

    private User readUser(ResultSet row) throws SQLException {
        final JSONArray interests = new JSONArray(row.getString("interested_communities"));
        final Set<String> ids = new LinkedHashSet<>();
        for (int i = 0; i < interests.length(); i++) {
            ids.add(interests.getString(i));
        }
        return new User(row.getString("user_id"), row.getString("username"),
                row.getString("password_hash"), row.getString("display_name"), row.getString("bio"),
                row.getString("contact_info"), WeekendStyle.valueOf(row.getString("weekend_style")), ids);
    }

    @Override
    public void updateUserProfile(User user) {
        final String update = "UPDATE users SET display_name=?, bio=?, contact_info=?, weekend_style=?, "
                + "interested_communities=? WHERE user_id=?";
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(update)) {
            final int idParameter = bindProfile(sql, 1, user);
            sql.setString(idParameter, user.getUserId());
            if (sql.executeUpdate() == 0) {
                throw new IllegalArgumentException("Unknown user ID.");
            }
        }
        catch (SQLException ex) {
            throw new IllegalStateException("Could not update profile.", ex);
        }
    }

    private int bindProfile(PreparedStatement sql, int first, User user) throws SQLException {
        return bindStrings(sql, first, user.getDisplayName(), user.getBio(), user.getContactInfo(),
                user.getWeekendStyle().name(), new JSONArray(user.getInterestedCommunityIds()).toString());
    }

    private int bindStrings(PreparedStatement sql, int first, String... values) throws SQLException {
        int parameter = first;
        for (String value : values) {
            sql.setString(parameter, value);
            parameter++;
        }
        return parameter;
    }
}
