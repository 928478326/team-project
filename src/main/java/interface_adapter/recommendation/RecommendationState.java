package interface_adapter.recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * UI state for the Profile &amp; 3+4 Buddy Recommendation view.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class RecommendationState {

    private String displayName = "";
    private String bio = "";
    private String contactInfo = "";
    private String utorid = "";
    private boolean recordedUtorid;
    private boolean shareContactInfo = true;
    private boolean uoftVerified;
    private String weekendStyleCode = "INDOOR_CHILL";
    private Set<String> interestedCommunityIds = Collections.emptySet();
    private List<RecommendationCard> recommendations = Collections.emptyList();
    private List<SuggestedEventRow> suggestedEvents = Collections.emptyList();
    private boolean aiGenerated;
    private String statusMessage = "";
    private String errorMessage = "";

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? "" : displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio == null ? "" : bio;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo == null ? "" : contactInfo;
    }

    void setPrivateStatus(String ownerUtorid, boolean recorded, boolean shared, boolean verified) {
        utorid = ownerUtorid;
        recordedUtorid = recorded;
        shareContactInfo = shared;
        uoftVerified = verified;
    }

    public String getUtorid() {
        return utorid;
    }

    /**
     * Indicates whether the owner has recorded a UTORid.
     * @return true if a UTORid is present
     */
    public boolean hasUtorid() {
        return recordedUtorid;
    }

    public boolean isShareContactInfo() {
        return shareContactInfo;
    }

    public boolean isUoftVerified() {
        return uoftVerified;
    }

    public String getWeekendStyleCode() {
        return weekendStyleCode;
    }

    public void setWeekendStyleCode(String weekendStyleCode) {
        this.weekendStyleCode =
                (weekendStyleCode == null || weekendStyleCode.isBlank())
                        ? "INDOOR_CHILL"
                        : weekendStyleCode;
    }

    public Set<String> getInterestedCommunityIds() {
        return interestedCommunityIds;
    }

    public void setInterestedCommunityIds(Set<String> interestedCommunityIds) {
        this.interestedCommunityIds = interestedCommunityIds == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new LinkedHashSet<>(interestedCommunityIds));
    }

    public List<RecommendationCard> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationCard> recommendations) {
        this.recommendations = recommendations == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(recommendations));
    }

    public List<SuggestedEventRow> getSuggestedEvents() {
        return suggestedEvents;
    }

    public void setSuggestedEvents(List<SuggestedEventRow> suggestedEvents) {
        this.suggestedEvents = suggestedEvents == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(suggestedEvents));
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage == null ? "" : statusMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? "" : errorMessage;
    }

    /**
     * Formatted view-model card for one recommended buddy.
     */
    public static final class RecommendationCard {

        private final String userId;
        private final String displayName;
        private final String contactInfo;
        private final String weekendStyleLabel;
        private final String totalScoreLabel;
        private final String breakdownLabel;
        private final List<String> sharedCommunities;
        private final int sharedEventCount;
        private final String averageRatingDisplay;
        private final boolean penalized;
        private final String explanation;
        private final String icebreaker;

        public RecommendationCard(
                String userId,
                String displayName,
                String contactInfo,
                String weekendStyleLabel,
                String totalScoreLabel,
                String breakdownLabel,
                List<String> sharedCommunities,
                int sharedEventCount,
                String averageRatingDisplay,
                boolean penalized,
                String explanation,
                String icebreaker) {
            this.userId = userId;
            this.displayName = displayName;
            this.contactInfo = contactInfo;
            this.weekendStyleLabel = weekendStyleLabel;
            this.totalScoreLabel = totalScoreLabel;
            this.breakdownLabel = breakdownLabel;
            this.sharedCommunities = sharedCommunities == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(sharedCommunities));
            this.sharedEventCount = sharedEventCount;
            this.averageRatingDisplay = averageRatingDisplay;
            this.penalized = penalized;
            this.explanation = explanation;
            this.icebreaker = icebreaker;
        }

        public String getUserId() {
            return userId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getContactInfo() {
            return contactInfo;
        }

        public String getWeekendStyleLabel() {
            return weekendStyleLabel;
        }

        public String getTotalScoreLabel() {
            return totalScoreLabel;
        }

        public String getBreakdownLabel() {
            return breakdownLabel;
        }

        public List<String> getSharedCommunities() {
            return sharedCommunities;
        }

        public int getSharedEventCount() {
            return sharedEventCount;
        }

        public String getAverageRatingDisplay() {
            return averageRatingDisplay;
        }

        public boolean isPenalized() {
            return penalized;
        }

        public String getExplanation() {
            return explanation;
        }

        public String getIcebreaker() {
            return icebreaker;
        }
    }

    /**
     * Formatted row describing a suggested upcoming event.
     */
    public static final class SuggestedEventRow {

        private final String eventId;
        private final String communityId;
        private final String title;
        private final String startsAtDisplay;

        public SuggestedEventRow(
                String eventId, String communityId, String title, String startsAtDisplay) {
            this.eventId = eventId;
            this.communityId = communityId;
            this.title = title;
            this.startsAtDisplay = startsAtDisplay;
        }

        public String getEventId() {
            return eventId;
        }

        public String getCommunityId() {
            return communityId;
        }

        public String getTitle() {
            return title;
        }

        public String getStartsAtDisplay() {
            return startsAtDisplay;
        }
    }
}
