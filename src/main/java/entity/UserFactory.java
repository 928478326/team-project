package entity;

/**
 * Factory interface for constructing new {@link User} entities with consistent defaults.
 * Applies the Factory pattern and Dependency Inversion Principle.
 */
public interface UserFactory {

    /**
     * Creates a newly registered user with default profile attributes.
     *
     * @param userId generated unique user ID
     * @param username raw username
     * @param passwordHash hashed password
     * @return newly created {@link User}
     */
    User create(String userId, String username, String passwordHash);
}
