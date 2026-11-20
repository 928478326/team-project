package entity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Core domain entity representing a CampusBuddy student user.
 * Immutable to protect enterprise invariants across layers.
 */
public final class User {

    private final String userId;
    private final String username;
    private final String passwordHash;
    private final String displayName;
    private final String bio;
    private final String contactInfo;
    private final WeekendStyle weekendStyle;
    private final Set<String> interestedCommunityIds;

    /**
     * Constructs an immutable {@code User} while enforcing core domain invariants.
     *
     * @param userId unique identifier (non-blank)
     * @param username login handle, normalized to trimmed lowercase (non-blank)
     * @param passwordHash hashed password (non-blank)
     * @param displayName visible name (non-blank)
     * @param bio optional short bio (null normalized to empty string)
     * @param contactInfo optional contact handle such as Discord/Instagram/email
     * @param weekendStyle preferred weekend style (non-null)
     * @param interestedCommunityIds set of community IDs the user is interested in
     */
    public User(
            String userId,
            String username,
            String passwordHash,
            String displayName,
            String bio,
            String contactInfo,
            WeekendStyle weekendStyle,
            Set<String> interestedCommunityIds) {
        this.userId = requireNonBlank(userId, "userId");
        this.username = normalizeUsername(username);
        this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
        this.displayName = requireNonBlank(displayName, "displayName");
        this.bio = bio == null ? "" : bio.trim();
        this.contactInfo = contactInfo == null ? "" : contactInfo.trim();
        this.weekendStyle = Objects.requireNonNull(weekendStyle, "weekendStyle must not be null");

        Set<String> normalizedCommunities = new LinkedHashSet<>();
        if (interestedCommunityIds != null) {
            for (String communityId : interestedCommunityIds) {
                if (communityId != null && !communityId.isBlank()) {
                    normalizedCommunities.add(communityId.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        this.interestedCommunityIds = Collections.unmodifiableSet(normalizedCommunities);
    }

    /**
     * Normalizes a raw username string to canonical lowercase form.
     *
     * @param rawUsername raw username text
     * @return trimmed lowercase username
     * @throws IllegalArgumentException if null or blank
     */
    public static String normalizeUsername(String rawUsername) {
        if (rawUsername == null || rawUsername.isBlank()) {
            throw new IllegalArgumentException("username must not be blank.");
        }
        return rawUsername.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns a new {@code User} with updated editable profile fields while preserving
     * {@code userId}, {@code username}, and {@code passwordHash}.
     *
     * @param newDisplayName updated display name (non-blank)
     * @param newBio updated bio text
     * @param newContactInfo updated contact info
     * @param newWeekendStyle updated weekend style (non-null)
     * @param newInterestedCommunityIds updated interested community IDs
     * @return updated immutable {@code User}
     */
    public User withProfile(
            String newDisplayName,
            String newBio,
            String newContactInfo,
            WeekendStyle newWeekendStyle,
            Set<String> newInterestedCommunityIds) {
        return new User(
                this.userId,
                this.username,
                this.passwordHash,
                newDisplayName,
                newBio,
                newContactInfo,
                newWeekendStyle,
                newInterestedCommunityIds);
    }

    /**
     * Checks whether the supplied hash matches this user's stored password hash.
     *
     * @param candidateHash hashed candidate password
     * @return {@code true} if the hashes match
     */
    public boolean matchesPasswordHash(String candidateHash) {
        return candidateHash != null && this.passwordHash.equals(candidateHash);
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public WeekendStyle getWeekendStyle() {
        return weekendStyle;
    }

    public Set<String> getInterestedCommunityIds() {
        return interestedCommunityIds;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value.trim();
    }
}
