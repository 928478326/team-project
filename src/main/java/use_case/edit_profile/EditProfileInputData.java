package use_case.edit_profile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable input data for loading or saving a student's Tier 1 (public) and Tier 2 (self-only)
 * profile fields.
 */
public final class EditProfileInputData {

    /** Supported profile operations. */
    public enum Action {
        LOAD,
        SAVE
    }

    private final String userId;
    private final Action action;
    private final String displayName;
    private final String bio;
    private final String contactInfo;
    private final String weekendStyleCode;
    private final Set<String> interestedCommunityIds;
    private final String utorid;
    private final boolean shareContactInfo;
    private final boolean privateProfileIncluded;

    /**
     * Constructs input data updating Tier 1 public profile fields (preserving existing Tier 2
     * private settings unless {@link #withPrivateSettings(String, boolean)} is chained).
     *
     * @param userId target user ID
     * @param action profile operation ({@link Action#LOAD} or {@link Action#SAVE})
     * @param displayName visible display name
     * @param bio public short bio
     * @param contactInfo contact handle
     * @param weekendStyleCode free-text weekend preference or preset code
     * @param interestedCommunityIds selected community IDs
     */
    public EditProfileInputData(
            String userId,
            Action action,
            String displayName,
            String bio,
            String contactInfo,
            String weekendStyleCode,
            Set<String> interestedCommunityIds) {
        this.userId = trimOrEmpty(userId);
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.displayName = emptyIfNull(displayName);
        this.bio = emptyIfNull(bio);
        this.contactInfo = emptyIfNull(contactInfo);
        this.weekendStyleCode = trimOrEmpty(weekendStyleCode);
        this.interestedCommunityIds = immutableSetCopy(interestedCommunityIds);
        this.utorid = "";
        this.shareContactInfo = true;
        this.privateProfileIncluded = false;
    }

    private EditProfileInputData(
            EditProfileInputData base, String utorid, boolean shareContactInfo) {
        this.userId = base.userId;
        this.action = base.action;
        this.displayName = base.displayName;
        this.bio = base.bio;
        this.contactInfo = base.contactInfo;
        this.weekendStyleCode = base.weekendStyleCode;
        this.interestedCommunityIds = base.interestedCommunityIds;
        this.utorid = trimOrEmpty(utorid);
        this.shareContactInfo = shareContactInfo;
        this.privateProfileIncluded = true;
    }

    /**
     * Returns a copy of this input data including Tier 2 self-only UTORid and contact privacy
     * preferences to apply during {@link Action#SAVE}.
     *
     * @param newUtorid self-only UofT UTORid input (blank to clear)
     * @param newShareContactInfo whether contact info may be shown on peer match cards
     * @return immutable {@code EditProfileInputData} carrying Tier 2 settings
     */
    public EditProfileInputData withPrivateSettings(
            String newUtorid, boolean newShareContactInfo) {
        return new EditProfileInputData(this, newUtorid, newShareContactInfo);
    }

    public String getUserId() {
        return userId;
    }

    public Action getAction() {
        return action;
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

    public boolean isShareContactInfo() {
        return shareContactInfo;
    }

    public boolean isPrivateProfileIncluded() {
        return privateProfileIncluded;
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

    private static String trimOrEmpty(String value) {
        final String result;
        if (value == null) {
            result = "";
        }
        else {
            result = value.trim();
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
