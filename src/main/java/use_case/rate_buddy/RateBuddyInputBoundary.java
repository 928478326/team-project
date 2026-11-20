package use_case.rate_buddy;

/**
 * Input boundary for submitting or updating a buddy rating.
 */
public interface RateBuddyInputBoundary {

    /**
     * Validates shared-event eligibility and upserts a buddy rating.
     *
     * @param inputData rating submission payload
     */
    void execute(RateBuddyInputData inputData);
}
