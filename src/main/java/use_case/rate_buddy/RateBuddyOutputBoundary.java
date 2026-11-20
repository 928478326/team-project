package use_case.rate_buddy;

/**
 * Output boundary for submitting or updating a buddy rating.
 */
public interface RateBuddyOutputBoundary {

    /**
     * Presents confirmation that the buddy rating was saved.
     *
     * @param outputData saved rating details
     */
    void prepareSuccessView(RateBuddyOutputData outputData);

    /**
     * Presents a validation or eligibility failure.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
