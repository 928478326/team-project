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
 * <p>Note: Whether {@link #getContactInfo()} is displayed to other students on match cards is
 * governed by {@link PrivateProfile#isShareContactInfo()}.
 */
public final class PublicProfile {

    /** Maximum allowed length for a visible display name. */
    public static final int MAX_DISPLAY_NAME_LENGTH = 80;

    /** Maximum allowed length for a public short bio. */
    public static final int MAX_BIO_LENGTH = 500;

    /** Maximum allowed length for a contact handle string. */
    public static final int MAX_CONTACT_LENGTH = 120;

    private final String displayName;
    private final String bio;
    private final String contactInfo;
    private final WeekendStyle weekendStyle;
    private final Set<String> interestedCommunityIds;

    /**
     * Constructs a validated immutable {@code PublicProfile}.
     *
     * @param displayName visible student name (non-blank, max {@value #MAX_DISPLAY_NAME_LENGTH} chars)
     * @param bio optional public bio (null normalized to empty string, max {@value #MAX_BIO_LENGTH} chars)
     * @param contactInfo optional contact handle (null normalized to empty, max {@value #MAX_CONTACT_LENGTH} chars)
     * @param weekendStyle preferred weekend style Value Object (non-null)
     * @param interestedCommunityIds set of community IDs normalized to lowercase
     * @throws IllegalArgumentException if any invariant is violated
     */
    public PublicProfile(
            String displayName,
            String bio,
            String contactInfo,
            WeekendStyle weekendStyle,
            Set<String> interestedCommunityIds) {
        this.displayName = sanitizeDisplayName(displayName);
        this.bio = sanitizeOptionalText(bio, MAX_BIO_LENGTH, "bio");
        this.contactInfo = sanitizeOptionalText(contactInfo, MAX_CONTACT_LENGTH, "contactInfo");
        this.weekendStyle = Objects.requireNonNull(weekendStyle, "weekendStyle must not be null");
        this.interestedCommunityIds = normalizeCommunities(interestedCommunityIds);
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

    private static String sanitizeDisplayName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank.");
        }
        final String trimmed = rawName.trim();
        if (trimmed.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "displayName must not exceed " + MAX_DISPLAY_NAME_LENGTH + " characters.");
        }
        return trimmed;
    }

    private static String sanitizeOptionalText(String raw, int maxLength, String fieldName) {
        final String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed " + maxLength + " characters.");
        }
        return trimmed;
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
