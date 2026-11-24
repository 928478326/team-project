package use_case.signup;

import entity.User;

/**
 * Narrow Data Access Interface for the Signup use case (ISP).
 */
public interface SignupDataAccessInterface {

    /**
     * Persists a newly created {@link User}.
     *
     * @param user newly constructed user entity
     * @return true if inserted, false if the username was already taken
     * @throws IllegalStateException if storage fails
     */
    boolean saveUser(User user);
}
