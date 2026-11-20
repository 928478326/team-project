package use_case.edit_profile;

import entity.User;
import java.util.Optional;

/**
 * Narrow Data Access Interface for loading and updating a user profile (ISP).
 */
public interface EditProfileDataAccessInterface {

    /**
     * Finds a user by ID.
     *
     * @param userId target user ID
     * @return user entity if found
     */
    Optional<User> findUserById(String userId);

    /**
     * Persists updated profile fields for an existing {@link User}.
     *
     * @param updatedUser immutable user object with updated profile fields
     */
    void updateUserProfile(User updatedUser);
}
