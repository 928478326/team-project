package use_case.edit_profile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Immutable output data for a student's editable profile.
 */
public final class EditProfileOutputData {

    private final String userId;
    private final String displayName;
    private final String bio;
    private final String contactInfo;
    private final String weekendStyleCode;
    private final Set<String> interestedCommunityIds;
    private final String statusMessage;

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
        this.bio = bio == null ? "" : bio;
        this.contactInfo = contactInfo == null ? "" : contactInfo;
        this.weekendStyleCode = weekendStyleCode;
        this.interestedCommunityIds = interestedCommunityIds == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new LinkedHashSet<>(interestedCommunityIds));
        this.statusMessage = statusMessage == null ? "" : statusMessage;
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

    public String getStatusMessage() {
        return statusMessage;
    }
}
