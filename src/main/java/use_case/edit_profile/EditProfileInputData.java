package use_case.edit_profile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable input data for loading or saving a student's profile.
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

    public EditProfileInputData(
            String userId,
            Action action,
            String displayName,
            String bio,
            String contactInfo,
            String weekendStyleCode,
            Set<String> interestedCommunityIds) {
        this.userId = userId == null ? "" : userId.trim();
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.displayName = displayName == null ? "" : displayName;
        this.bio = bio == null ? "" : bio;
        this.contactInfo = contactInfo == null ? "" : contactInfo;
        this.weekendStyleCode = weekendStyleCode == null ? "" : weekendStyleCode.trim();
        this.interestedCommunityIds = interestedCommunityIds == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new LinkedHashSet<>(interestedCommunityIds));
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
}
