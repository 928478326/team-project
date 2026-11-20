package use_case.recommend_buddies;

/**
 * Immutable input data for buddy recommendations.
 */
public final class RecommendBuddiesInputData {

    /** Default number of top buddy candidates to return. */
    public static final int DEFAULT_MAX_RESULTS = 5;

    private final String userId;
    private final boolean hideDoNotMeetAgain;
    private final int maxResults;

    public RecommendBuddiesInputData(String userId, boolean hideDoNotMeetAgain, int maxResults) {
        this.userId = userId == null ? "" : userId.trim();
        this.hideDoNotMeetAgain = hideDoNotMeetAgain;
        this.maxResults = maxResults <= 0 ? DEFAULT_MAX_RESULTS : maxResults;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isHideDoNotMeetAgain() {
        return hideDoNotMeetAgain;
    }

    public int getMaxResults() {
        return maxResults;
    }
}
