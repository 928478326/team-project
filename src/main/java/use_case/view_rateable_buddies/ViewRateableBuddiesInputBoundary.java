package use_case.view_rateable_buddies;

/**
 * Input boundary for listing buddies who share an event with the current user.
 */
public interface ViewRateableBuddiesInputBoundary {

    /**
     * Loads all buddy-event pairs eligible for rating by {@code inputData.getUserId()}.
     *
     * @param inputData active user ID
     */
    void execute(ViewRateableBuddiesInputData inputData);
}
