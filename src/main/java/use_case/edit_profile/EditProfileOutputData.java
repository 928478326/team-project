package use_case.edit_profile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Immutable output data for a student's own editable profile view, containing Tier 1 (public)
 * and Tier 2 (self-only) fields while strictly excluding Tier 3 ({@code LlmPersona}) and
 * authentication secrets.
 */
public final class EditProfileOutputData {

    private final String userId;
    private final String displayName;
    private final String bio;
    private final String contactInfo;
    private final String weekendStyleCode;
    private final Set<String> interestedCommunityIds;
    private final String utorid;
    private final boolean hasUtorid;
    private final boolean shareContactInfo;
    private final boolean uoftVerified;
    private final String statusMessage;

    /**
     * Constructs output data projecting Tier 1 fields with default unverified Tier 2 status.
     *
     * @param userId owning user ID
     * @param displayName visible display name
     * @param bio public short bio
     * @param contactInfo contact handle
     * @param weekendStyleCode serialized weekend style text or preset code
     * @param interestedCommunityIds selected community IDs
     * @param statusMessage human-readable status message
     */
    public EditProfileOutputData(
            String userId,
            String displayName,
            String bio,
            String contactInfo,
            String weekendStyleCode,
            Set<String> interestedCommunityIds,
            String statusMessage) {
        this.userId = userId;
        this.displayName = displayName;
        this.bio = emptyIfNull(bio);
        this.contactInfo = emptyIfNull(contactInfo);
        this.weekendStyleCode = weekendStyleCode;
        this.interestedCommunityIds = immutableSetCopy(interestedCommunityIds);
        this.utorid = "";
        this.hasUtorid = false;
        this.shareContactInfo = true;
        this.uoftVerified = false;
        this.statusMessage = emptyIfNull(statusMessage);
    }

    private EditProfileOutputData(
            EditProfileOutputData base,
            String utorid,
            boolean hasUtorid,
            boolean shareContactInfo,
            boolean uoftVerified) {
        this.userId = base.userId;
        this.displayName = base.displayName;
        this.bio = base.bio;
        this.contactInfo = base.contactInfo;
        this.weekendStyleCode = base.weekendStyleCode;
        this.interestedCommunityIds = base.interestedCommunityIds;
        this.utorid = emptyIfNull(utorid);
        this.hasUtorid = hasUtorid;
        this.shareContactInfo = shareContactInfo;
        this.uoftVerified = uoftVerified;
        this.statusMessage = base.statusMessage;
    }

    /**
     * Returns an enriched copy carrying the owning user's Tier 2 self-only privacy and
     * verification state.
     *
     * @param selfUtorid self-only UTORid string
     * @param recordedUtorid whether a non-empty UTORid is recorded
     * @param contactShared whether contact info is shared on peer cards
     * @param verifiedStatus whether UofT student ownership has been verified
     * @return immutable {@code EditProfileOutputData} including Tier 2 state
     */
    public EditProfileOutputData withPrivateStatus(
            String selfUtorid,
            boolean recordedUtorid,
            boolean contactShared,
            boolean verifiedStatus) {
        return new EditProfileOutputData(
                this, selfUtorid, recordedUtorid, contactShared, verifiedStatus);
    }

    public String getUserId() {
        return userId;
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

    public String getWeekendStyleCode() {
        return weekendStyleCode;
    }

    public Set<String> getInterestedCommunityIds() {
        return interestedCommunityIds;
    }

    public String getUtorid() {
        return utorid;
    }

    /**
     * Indicates whether the user has recorded a non-empty UTORid on their profile.
     *
     * @return {@code true} if a UTORid is recorded
     */
    public boolean hasUtorid() {
        return hasUtorid;
    }

    public boolean isShareContactInfo() {
        return shareContactInfo;
    }

    public boolean isUoftVerified() {
        return uoftVerified;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    private static String emptyIfNull(String value) {
        final String result;
        if (value == null) {
            result = "";
        }
        else {
            result = value;
        }
        return result;
    }

    private static Set<String> immutableSetCopy(Set<String> source) {
        final Set<String> copy;
        if (source == null) {
            copy = Collections.emptySet();
        }
        else {
            copy = Collections.unmodifiableSet(new LinkedHashSet<>(source));
        }
        return copy;
    }
}
