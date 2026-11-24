package use_case.edit_profile;

import java.util.Optional;

import entity.User;

/**
 * Narrow Data Access Interface for loading and updating a user profile (ISP).
 */
public interface EditProfileDataAccessInterface {

    /**
     * Finds a user by ID.
     *
     * @param userId target user ID
     * @return user entity if found
     * @throws IllegalStateException if storage cannot read the profile
     */
    Optional<User> findUserById(String userId);

    /**
     * Persists updated profile fields for an existing {@link User}.
     *
     * @param updatedUser immutable user object with updated profile fields
     * @throws IllegalStateException if storage cannot write the profile
     */
    void updateUserProfile(User updatedUser);
}
