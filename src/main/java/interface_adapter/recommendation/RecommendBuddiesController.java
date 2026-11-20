package interface_adapter.recommendation;

import java.util.Objects;
import use_case.recommend_buddies.RecommendBuddiesInputBoundary;
import use_case.recommend_buddies.RecommendBuddiesInputData;

/**
 * Controller for generating 3+4 buddy recommendations.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class RecommendBuddiesController {

    private final RecommendBuddiesInputBoundary interactor;

    public RecommendBuddiesController(RecommendBuddiesInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    /**
     * Requests personalized recommendations for {@code userId}.
     *
     * @param userId active user ID
     * @param hideDoNotMeetAgain whether to exclude negatively rated peers
     * @param maxResults maximum number of buddy recommendations to return
     */
    public void execute(String userId, boolean hideDoNotMeetAgain, int maxResults) {
        interactor.execute(new RecommendBuddiesInputData(userId, hideDoNotMeetAgain, maxResults));
    }
}
