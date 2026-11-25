package entity;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Core domain entity representing a CampusBuddy student user, organized around a strict
 * 3-Tier Profile Visibility Model.
 *
 * <ol>
 *   <li>{@link PublicProfile} — visible to the student and to peers on campus</li>
 *   <li>{@link PrivateProfile} — visible &amp; editable ONLY by the student themselves
 *       (e.g., UofT {@code utorid} entry, verification state, and contact privacy preferences)</li>
 *   <li>{@link LlmPersona} + {@code passwordHash} — strictly internal system/LLM state,
 *       invisible to all UI views including the student themselves</li>
 * </ol>
 */
public final class User {

    private final String userId;
    private final String username;
    private final String passwordHash;
    private final PublicProfile publicProfile;
    private final PrivateProfile privateProfile;
    private final LlmPersona llmPersona;

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
        this(userId, username, passwordHash,
                new PublicProfile(displayName, bio, contactInfo, weekendStyle, interestedCommunityIds));
    }

    /**
     * Constructs an immutable {@code User} with default self-only {@link PrivateProfile}
     * and empty {@link LlmPersona}.
     *
     * @param userId unique identifier (non-blank)
     * @param username login handle, normalized to trimmed lowercase (non-blank)
     * @param passwordHash hashed password (non-blank)
     * @param publicProfile Tier 1 public profile (non-null)
     */
    public User(
            String userId,
            String username,
            String passwordHash,
            PublicProfile publicProfile) {
        this(
                userId,
                username,
                passwordHash,
                publicProfile,
                PrivateProfile.defaultUnverified(),
                LlmPersona.empty());
    }

    /**
     * Canonical constructor assembling all three profile visibility tiers explicitly.
     *
     * @param userId unique identifier (non-blank)
     * @param username login handle, normalized to trimmed lowercase (non-blank)
     * @param passwordHash hashed password (non-blank)
     * @param publicProfile Tier 1 public profile (non-null)
     * @param privateProfile Tier 2 self-only private &amp; verification profile (non-null)
     * @param llmPersona Tier 3 system-maintained hidden LLM persona (non-null)
     */
    public User(
            String userId,
            String username,
            String passwordHash,
            PublicProfile publicProfile,
            PrivateProfile privateProfile,
            LlmPersona llmPersona) {
        this.userId = requireNonBlank(userId, "userId");
        this.username = normalizeUsername(username);
        this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
        this.publicProfile = Objects.requireNonNull(publicProfile, "publicProfile must not be null");
        this.privateProfile =
                Objects.requireNonNull(privateProfile, "privateProfile must not be null");
        this.llmPersona = Objects.requireNonNull(llmPersona, "llmPersona must not be null");
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
     * Returns a new {@code User} with updated Tier 1 public profile fields while preserving
     * identity, {@link PrivateProfile}, and {@link LlmPersona}.
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
        final PublicProfile updatedPublic = new PublicProfile(
                newDisplayName,
                newBio,
                newContactInfo,
                newWeekendStyle,
                newInterestedCommunityIds);
        return new User(
                this.userId,
                this.username,
                this.passwordHash,
                updatedPublic,
                this.privateProfile,
                this.llmPersona);
    }

    /**
     * Returns a new {@code User} updating both user-editable tiers ({@link PublicProfile} and
     * self-only {@link PrivateProfile}) while strictly preserving {@link LlmPersona}.
     *
     * @param newPublicProfile updated Tier 1 public profile (non-null)
     * @param newPrivateProfile updated Tier 2 self-only profile (non-null)
     * @return updated immutable {@code User}
     */
    public User withEditableProfile(
            PublicProfile newPublicProfile, PrivateProfile newPrivateProfile) {
        return new User(
                this.userId,
                this.username,
                this.passwordHash,
                newPublicProfile,
                newPrivateProfile,
                this.llmPersona);
    }

    /**
     * Returns a new {@code User} with updated Tier 2 self-only privacy/verification settings
     * while preserving {@link PublicProfile} and {@link LlmPersona}.
     *
     * @param newPrivateProfile updated Tier 2 private profile (non-null)
     * @return updated immutable {@code User}
     */
    public User withPrivateProfile(PrivateProfile newPrivateProfile) {
        return new User(
                this.userId,
                this.username,
                this.passwordHash,
                this.publicProfile,
                newPrivateProfile,
                this.llmPersona);
    }

    /**
     * Domain entry point for LLM / backend profile enrichment: returns a new {@code User} with
     * an updated hidden {@link LlmPersona} while preserving all user-visible profile tiers.
     *
     * @param newLlmPersona updated Tier 3 hidden persona (non-null)
     * @return updated immutable {@code User}
     */
    public User withLlmPersona(LlmPersona newLlmPersona) {
        return new User(
                this.userId,
                this.username,
                this.passwordHash,
                this.publicProfile,
                this.privateProfile,
                newLlmPersona);
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

    /**
     * Checks whether the stored profile still matches the snapshot an edit started from.
     * @param other expected account and profile
     * @return whether every editable field and the stored persona still match
     */
    public boolean hasSameProfileAs(User other) {
        return userId.equals(other.userId) && publicProfile.equals(other.publicProfile)
                && getWeekendStyle().name().equals(other.getWeekendStyle().name())
                && privateProfile.equals(other.privateProfile) && llmPersona.equals(other.llmPersona);
    }

    public PublicProfile getPublicProfile() {
        return publicProfile;
    }

    public PrivateProfile getPrivateProfile() {
        return privateProfile;
    }

    public LlmPersona getLlmPersona() {
        return llmPersona;
    }

    public String getDisplayName() {
        return publicProfile.getDisplayName();
    }

    public String getBio() {
        return publicProfile.getBio();
    }

    public String getContactInfo() {
        return publicProfile.getContactInfo();
    }

    /**
     * Returns the peer-safe contact display string honoring {@link PrivateProfile#isShareContactInfo()}.
     *
     * @return raw contact handle if sharing is enabled, otherwise privacy placeholder
     */
    public String getPublicContactInfo() {
        return privateProfile.maskContactInfo(publicProfile.getContactInfo());
    }

    /**
     * Returns whether this user has recorded a non-empty UTORid on their self-only profile.
     *
     * @return {@code true} if a UTORid has been entered
     */
    public boolean hasUtorid() {
        return privateProfile.hasUtorid();
    }

    /**
     * Returns whether this user's UofT student status has been confirmed by a trusted verification
     * workflow (safe to display on peer cards without revealing the underlying UTORid).
     *
     * @return {@code true} only if verified
     */
    public boolean isUoftVerified() {
        return privateProfile.isUoftVerified();
    }

    public WeekendStyle getWeekendStyle() {
        return publicProfile.getWeekendStyle();
    }

    public Set<String> getInterestedCommunityIds() {
        return publicProfile.getInterestedCommunityIds();
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value.trim();
    }
}
