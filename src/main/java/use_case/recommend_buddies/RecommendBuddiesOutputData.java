package use_case.recommend_buddies;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable output data for 3+4 buddy recommendations and suggested upcoming events.
 */
public final class RecommendBuddiesOutputData {

    private final List<BuddyRecommendationItem> recommendations;
    private final List<SuggestedEventItem> suggestedEvents;
    private final boolean aiGenerated;
    private final String statusMessage;

    public RecommendBuddiesOutputData(
            List<BuddyRecommendationItem> recommendations,
            List<SuggestedEventItem> suggestedEvents,
            boolean aiGenerated,
            String statusMessage) {
        this.recommendations = recommendations == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(recommendations));
        this.suggestedEvents = suggestedEvents == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(suggestedEvents));
        this.aiGenerated = aiGenerated;
        this.statusMessage = statusMessage == null ? "" : statusMessage;
    }

    public List<BuddyRecommendationItem> getRecommendations() {
        return recommendations;
    }

    public List<SuggestedEventItem> getSuggestedEvents() {
        return suggestedEvents;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Immutable DTO describing one recommended buddy card.
     */
    public static final class BuddyRecommendationItem {

        private final String userId;
        private final String displayName;
        private final String contactInfo;
        private final String weekendStyleLabel;
        private final double totalScore;
        private final double styleScore;
        private final double interestAndHistoryScore;
        private final double ratingScore;
        private final List<String> sharedCommunities;
        private final int sharedEventCount;
        private final String averageRatingDisplay;
        private final boolean penalized;
        private final String explanation;
        private final String icebreaker;

        public BuddyRecommendationItem(
                String userId,
                String displayName,
                String contactInfo,
                String weekendStyleLabel,
                double totalScore,
                double styleScore,
                double interestAndHistoryScore,
                double ratingScore,
                List<String> sharedCommunities,
                int sharedEventCount,
                String averageRatingDisplay,
                boolean penalized,
                String explanation,
                String icebreaker) {
            this.userId = userId;
            this.displayName = displayName;
            this.contactInfo = contactInfo == null ? "" : contactInfo;
            this.weekendStyleLabel = weekendStyleLabel;
            this.totalScore = totalScore;
            this.styleScore = styleScore;
            this.interestAndHistoryScore = interestAndHistoryScore;
            this.ratingScore = ratingScore;
            this.sharedCommunities = sharedCommunities == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(sharedCommunities));
            this.sharedEventCount = sharedEventCount;
            this.averageRatingDisplay = averageRatingDisplay == null ? "No ratings yet" : averageRatingDisplay;
            this.penalized = penalized;
            this.explanation = explanation == null ? "" : explanation;
            this.icebreaker = icebreaker == null ? "" : icebreaker;
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

        public double getTotalScore() {
            return totalScore;
        }

        public double getStyleScore() {
            return styleScore;
        }

        public double getInterestAndHistoryScore() {
            return interestAndHistoryScore;
        }

        public double getRatingScore() {
            return ratingScore;
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
     * Immutable DTO describing a suggested upcoming event matching the user's interests.
     */
    public static final class SuggestedEventItem {

        private final String eventId;
        private final String communityId;
        private final String title;
        private final Instant startsAt;

        public SuggestedEventItem(
                String eventId, String communityId, String title, Instant startsAt) {
            this.eventId = eventId;
            this.communityId = communityId;
            this.title = title;
            this.startsAt = startsAt;
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

        public Instant getStartsAt() {
            return startsAt;
        }
    }
}
