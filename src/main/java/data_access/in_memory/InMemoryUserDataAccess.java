package data_access.in_memory;

import entity.User;
import java.util.Objects;
import java.util.Optional;
import use_case.edit_profile.EditProfileDataAccessInterface;
import use_case.login.LoginDataAccessInterface;
import use_case.signup.SignupDataAccessInterface;

/**
 * In-memory user DAO implementing auth and profile Data Access Interfaces.
 */
public final class InMemoryUserDataAccess
        implements SignupDataAccessInterface,
                LoginDataAccessInterface,
                EditProfileDataAccessInterface {

    private final InMemoryCampusBuddyStore store;

    public InMemoryUserDataAccess(InMemoryCampusBuddyStore store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    public boolean existsByUsername(String normalizedUsername) {
        synchronized (store) {
            return store.userIdByUsername().containsKey(normalizedUsername);
        }
    }

    @Override
    public boolean saveUser(User user) {
        synchronized (store) {
            if (existsByUsername(user.getUsername())) {
                return false;
            }
            store.putUser(user);
            return true;
        }
    }

    @Override
    public Optional<User> findByUsername(String normalizedUsername) {
        synchronized (store) {
            String userId = store.userIdByUsername().get(normalizedUsername);
            if (userId == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(store.usersById().get(userId));
        }
    }

    @Override
    public Optional<User> findUserById(String userId) {
        synchronized (store) {
            return Optional.ofNullable(store.usersById().get(userId));
        }
    }

    @Override
    public void updateUserProfile(User updatedUser) {
        synchronized (store) {
            if (!store.usersById().containsKey(updatedUser.getUserId())) {
                throw new IllegalArgumentException("Unknown userId: " + updatedUser.getUserId());
            }
            store.putUser(updatedUser);
        }
    }
}
