package data_access.in_memory;

import java.util.Objects;
import java.util.Optional;

import entity.User;
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

    /**
     * Checks whether this store contains the given username.
     * @param normalizedUsername normalized login name
     * @return whether the username is in use
     */
    public boolean existsByUsername(String normalizedUsername) {
        synchronized (store) {
            return store.userIdByUsername().containsKey(normalizedUsername);
        }
    }

    @Override
    public boolean saveUser(User user) {
        synchronized (store) {
            final boolean available = !existsByUsername(user.getUsername());
            if (available) {
                if (store.usersById().containsKey(user.getUserId())) {
                    throw new IllegalStateException("User ID is already in use.");
                }
                store.putUser(user);
            }
            return available;
        }
    }

    @Override
    public Optional<User> findByUsername(String normalizedUsername) {
        synchronized (store) {
            final String userId = store.userIdByUsername().get(normalizedUsername);
            return Optional.ofNullable(userId).map(store.usersById()::get);
        }
    }

    @Override
    public Optional<User> findUserById(String userId) {
        synchronized (store) {
            return Optional.ofNullable(store.usersById().get(userId));
        }
    }

    @Override
    public boolean updateIfUnchanged(User expected, User updated) {
        synchronized (store) {
            final User saved = store.usersById().get(updated.getUserId());
            final boolean matches = saved != null && saved.hasSameProfileAs(expected);
            if (matches) {
                store.putUser(updated);
            }
            return matches;
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
