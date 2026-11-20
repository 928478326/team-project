package data_access.sqlite;

import entity.User;
import java.util.Objects;
import java.util.Optional;
import use_case.edit_profile.EditProfileDataAccessInterface;
import use_case.login.LoginDataAccessInterface;
import use_case.signup.SignupDataAccessInterface;

/**
 * SQLite implementation of user account and profile Data Access Interfaces.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class SqliteUserDataAccess
        implements SignupDataAccessInterface,
                LoginDataAccessInterface,
                EditProfileDataAccessInterface {

    private final SqliteDatabase database;

    public SqliteUserDataAccess(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database, "database must not be null");
    }

    @Override
    public boolean existsByUsername(String normalizedUsername) {
        // TODO [Track A]: SELECT 1 FROM users WHERE username = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteUserDataAccess.existsByUsername");
    }

    @Override
    public void saveUser(User user) {
        // TODO [Track A]: INSERT INTO users (...) VALUES (...)
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteUserDataAccess.saveUser");
    }

    @Override
    public Optional<User> findByUsername(String normalizedUsername) {
        // TODO [Track A]: SELECT * FROM users WHERE username = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteUserDataAccess.findByUsername");
    }

    @Override
    public Optional<User> findUserById(String userId) {
        // TODO [Track A]: SELECT * FROM users WHERE user_id = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteUserDataAccess.findUserById");
    }

    @Override
    public void updateUserProfile(User updatedUser) {
        // TODO [Track A]: UPDATE users SET display_name=?, bio=?, contact_info=?,
        // weekend_style=?, interested_communities=? WHERE user_id=?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteUserDataAccess.updateUserProfile");
    }
}
