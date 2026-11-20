package use_case.browse_events;

/**
 * Immutable input data for browsing community events.
 */
public final class BrowseEventsInputData {

    /** Filter constant indicating all communities should be included. */
    public static final String ALL_COMMUNITIES = "ALL";

    private final String currentUserId;
    private final String communityId;

    public BrowseEventsInputData(String currentUserId, String communityId) {
        this.currentUserId = currentUserId == null ? "" : currentUserId.trim();
        this.communityId = (communityId == null || communityId.isBlank())
                ? ALL_COMMUNITIES
                : communityId.trim();
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCommunityId() {
        return communityId;
    }
}
