package use_case.recommend_buddies;

/**
 * Input boundary for generating personalized 3+4 buddy recommendations with AI explanations.
 */
public interface RecommendBuddiesInputBoundary {

    /**
     * Computes ranked buddy recommendations and suggested upcoming events for a user.
     *
     * @param inputData recommendation parameters
     */
    void execute(RecommendBuddiesInputData inputData);
}
