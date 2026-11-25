package entity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable Value Object representing the <b>publicly visible</b> portion of a student's profile
 * (Tier 1 visibility: visible to the user and to matched peers on campus).
 *
 * <p>To keep pre-existing or newly registered accounts readable regardless of initial username
 * length, the constructor enforces core non-blank/null-safety invariants without rejecting legacy
 * field lengths; new profile edits validate UI input bounds via {@link #createValidated}.
 */
public final class PublicProfile {

    /** Maximum allowed length for a newly edited display name. */
    public static final int MAX_DISPLAY_NAME_LENGTH = 80;

    /** Maximum allowed length for a newly edited public short bio. */
    public static final int MAX_BIO_LENGTH = 500;

    /** Maximum allowed length for a newly edited contact handle string. */
    public static final int MAX_CONTACT_LENGTH = 120;

    private final String displayName;
    private final String bio;
    private final String contactInfo;
    private final WeekendStyle weekendStyle;
    private final Set<String> interestedCommunityIds;

    /**
     * Constructs an immutable {@code PublicProfile} while preserving readable existing records.
     *
     * @param displayName visible student name (non-blank)
     * @param bio optional public bio (null normalized to empty string)
     * @param contactInfo optional contact handle (null normalized to empty string)
     * @param weekendStyle preferred weekend style Value Object (non-null)
     * @param interestedCommunityIds set of community IDs normalized to lowercase
     * @throws IllegalArgumentException if {@code displayName} is null/blank or {@code weekendStyle} is null
     */
    public PublicProfile(
            String displayName,
            String bio,
            String contactInfo,
            WeekendStyle weekendStyle,
            Set<String> interestedCommunityIds) {
        this.displayName = requireNonBlankName(displayName);
        this.bio = trimOrEmpty(bio);
        this.contactInfo = trimOrEmpty(contactInfo);
        this.weekendStyle = Objects.requireNonNull(weekendStyle, "weekendStyle must not be null");
        this.interestedCommunityIds = normalizeCommunities(interestedCommunityIds);
    }

    /**
     * Validates and constructs a {@code PublicProfile} for new user-submitted profile edits,
     * enforcing field length limits before persistence.
     *
     * @param displayName visible student name (non-blank, max {@value #MAX_DISPLAY_NAME_LENGTH} chars)
     * @param bio optional public bio (max {@value #MAX_BIO_LENGTH} chars)
     * @param contactInfo optional contact handle (max {@value #MAX_CONTACT_LENGTH} chars)
     * @param weekendStyle preferred weekend style Value Object (non-null)
     * @param interestedCommunityIds set of community IDs normalized to lowercase
     * @return validated {@code PublicProfile}
     * @throws IllegalArgumentException if blank or any length limit is exceeded
     */
    public static PublicProfile createValidated(
            String displayName,
            String bio,
            String contactInfo,
            WeekendStyle weekendStyle,
            Set<String> interestedCommunityIds) {
        final PublicProfile candidate = new PublicProfile(
                displayName, bio, contactInfo, weekendStyle, interestedCommunityIds);
        enforceMaxLength(candidate.displayName, MAX_DISPLAY_NAME_LENGTH, "displayName");
        enforceMaxLength(candidate.bio, MAX_BIO_LENGTH, "bio");
        enforceMaxLength(candidate.contactInfo, MAX_CONTACT_LENGTH, "contactInfo");
        return candidate;
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

    @Override
    public boolean equals(Object obj) {
        final boolean equal;
        if (this == obj) {
            equal = true;
        }
        else if (!(obj instanceof PublicProfile)) {
            equal = false;
        }
        else {
            final PublicProfile that = (PublicProfile) obj;
            equal = displayName.equals(that.displayName)
                    && bio.equals(that.bio)
                    && contactInfo.equals(that.contactInfo)
                    && weekendStyle.equals(that.weekendStyle)
                    && interestedCommunityIds.equals(that.interestedCommunityIds);
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, bio, contactInfo, weekendStyle, interestedCommunityIds);
    }

    private static String requireNonBlankName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank.");
        }
        return rawName.trim();
    }

    private static String trimOrEmpty(String rawText) {
        final String sanitized;
        if (rawText == null) {
            sanitized = "";
        }
        else {
            sanitized = rawText.trim();
        }
        return sanitized;
    }

    private static void enforceMaxLength(String value, int maxLength, String fieldName) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed " + maxLength + " characters.");
        }
    }

    private static Set<String> normalizeCommunities(Set<String> source) {
        final Set<String> normalized = new LinkedHashSet<>();
        if (source != null) {
            for (String communityId : source) {
                if (communityId != null && !communityId.isBlank()) {
                    normalized.add(communityId.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return Collections.unmodifiableSet(normalized);
    }
}
