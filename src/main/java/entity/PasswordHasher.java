package entity;

/**
 * Abstraction for hashing raw user passwords before storage or verification.
 * Decouples domain and use-case policies from concrete hashing implementations (DIP).
 */
public interface PasswordHasher {

    /**
     * Hashes the supplied plaintext password for storage.
     *
     * @param rawPassword plaintext password (must not be null)
     * @return encoded password hash
     */
    String hash(String rawPassword);

    /**
     * Verifies a password against the encoding produced by this hasher.
     * @param rawPassword password entered at login
     * @param encoded stored credential
     * @return whether the supplied password matches
     */
    default boolean matches(String rawPassword, String encoded) {
        return hash(rawPassword).equals(encoded);
    }
}
