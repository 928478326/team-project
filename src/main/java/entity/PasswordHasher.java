package entity;

/**
 * Abstraction for hashing raw user passwords before storage or verification.
 * Decouples domain and use-case policies from concrete hashing implementations (DIP).
 */
public interface PasswordHasher {

    /**
     * Hashes the supplied plaintext password deterministically.
     *
     * @param rawPassword plaintext password (must not be null)
     * @return encoded password hash
     */
    String hash(String rawPassword);
}
