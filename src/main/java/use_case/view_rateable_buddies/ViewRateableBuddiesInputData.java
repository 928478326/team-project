package use_case.view_rateable_buddies;

/**
 * Immutable input data for viewing rateable buddies.
 */
public final class ViewRateableBuddiesInputData {

    private final String userId;

    public ViewRateableBuddiesInputData(String userId) {
        this.userId = userId == null ? "" : userId.trim();
    }

    public String getUserId() {
        return userId;
    }
}
