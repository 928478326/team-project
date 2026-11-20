package use_case.recommend_buddies;

/**
 * Output boundary for personalized buddy recommendations.
 */
public interface RecommendBuddiesOutputBoundary {

    /**
     * Presents ranked buddy recommendations and suggested events.
     *
     * @param outputData recommendation DTO payload
     */
    void prepareSuccessView(RecommendBuddiesOutputData outputData);

    /**
     * Presents an error when recommendations cannot be generated.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
