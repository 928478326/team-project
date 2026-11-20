package use_case.view_rateable_buddies;

/**
 * Output boundary for viewing rateable buddies.
 */
public interface ViewRateableBuddiesOutputBoundary {

    /**
     * Presents the list of co-participants eligible for rating.
     *
     * @param outputData rateable buddy items
     */
    void prepareSuccessView(ViewRateableBuddiesOutputData outputData);

    /**
     * Presents a query or validation error.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
