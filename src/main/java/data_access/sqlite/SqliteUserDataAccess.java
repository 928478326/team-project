package data_access.sqlite;

import entity.User;
import entity.WeekendStyle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.json.JSONArray;
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
        String insert = "INSERT INTO users (user_id, username, password_hash, display_name, bio, "
                + "contact_info, weekend_style, interested_communities) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(username) DO NOTHING";
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(insert)) {
            sql.setString(1, user.getUserId());
            sql.setString(2, user.getUsername());
            sql.setString(3, user.getPasswordHash());
            bindProfile(sql, 4, user);
            return sql.executeUpdate() == 1;
        } catch (SQLException ex) {
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
                if (!row.next()) {
                    return Optional.empty();
                }
                JSONArray interests = new JSONArray(row.getString("interested_communities"));
                Set<String> ids = new LinkedHashSet<>();
                for (int i = 0; i < interests.length(); i++) {
                    ids.add(interests.getString(i));
                }
                return Optional.of(new User(row.getString("user_id"), row.getString("username"),
                        row.getString("password_hash"), row.getString("display_name"), row.getString("bio"),
                        row.getString("contact_info"), WeekendStyle.valueOf(row.getString("weekend_style")), ids));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not read account.", ex);
        }
    }

    @Override
    public void updateUserProfile(User user) {
        String update = "UPDATE users SET display_name=?, bio=?, contact_info=?, weekend_style=?, "
                + "interested_communities=? WHERE user_id=?";
        try (Connection connection = database.openConnection();
                PreparedStatement sql = connection.prepareStatement(update)) {
            bindProfile(sql, 1, user);
            sql.setString(6, user.getUserId());
            if (sql.executeUpdate() == 0) {
                throw new IllegalArgumentException("Unknown user ID.");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not update profile.", ex);
        }
    }

    private void bindProfile(PreparedStatement sql, int first, User user) throws SQLException {
        sql.setString(first, user.getDisplayName());
        sql.setString(first + 1, user.getBio());
        sql.setString(first + 2, user.getContactInfo());
        sql.setString(first + 3, user.getWeekendStyle().name());
        sql.setString(first + 4, new JSONArray(user.getInterestedCommunityIds()).toString());
    }
}
