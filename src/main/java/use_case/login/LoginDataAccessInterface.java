package use_case.login;

import entity.User;
import java.util.Optional;

/**
 * Narrow Data Access Interface for the Login use case (ISP).
 */
public interface LoginDataAccessInterface {

    /**
     * Looks up a user by normalized lowercase username.
     *
     * @param normalizedUsername canonical lowercase username
     * @return matching {@link User} if found
     */
    Optional<User> findByUsername(String normalizedUsername);
}
