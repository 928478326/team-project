package use_case.signup;

import entity.User;

/**
 * Narrow Data Access Interface for the Signup use case (ISP).
 */
public interface SignupDataAccessInterface {

    /**
     * Checks whether a user with the normalized username already exists.
     *
     * @param normalizedUsername canonical lowercase username
     * @return {@code true} if taken
     */
    boolean existsByUsername(String normalizedUsername);

    /**
     * Persists a newly created {@link User}.
     *
     * @param user newly constructed user entity
     */
    void saveUser(User user);
}
