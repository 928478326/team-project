package use_case.recommend_buddies;

import entity.BuddyScoringStrategy;
import java.time.Clock;
import java.util.Objects;

/**
 * Interactor implementing the 3+4 buddy recommendation pipeline with resilient LLM explanations.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class RecommendBuddiesInteractor implements RecommendBuddiesInputBoundary {

    /** Maximum number of suggested upcoming events included alongside buddy cards. */
    public static final int MAX_SUGGESTED_EVENTS = 3;

    private final RecommendBuddiesDataAccessInterface dataAccess;
    private final BuddyScoringStrategy scoringStrategy;
    private final LlmRecommendationGateway primaryLlmGateway;
    private final LlmRecommendationGateway fallbackGateway;
    private final RecommendBuddiesOutputBoundary outputBoundary;
    private final Clock clock;

    public RecommendBuddiesInteractor(
            RecommendBuddiesDataAccessInterface dataAccess,
            BuddyScoringStrategy scoringStrategy,
            LlmRecommendationGateway primaryLlmGateway,
            LlmRecommendationGateway fallbackGateway,
            RecommendBuddiesOutputBoundary outputBoundary,
            Clock clock) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.scoringStrategy =
                Objects.requireNonNull(scoringStrategy, "scoringStrategy must not be null");
        this.primaryLlmGateway =
                Objects.requireNonNull(primaryLlmGateway, "primaryLlmGateway must not be null");
        this.fallbackGateway =
                Objects.requireNonNull(fallbackGateway, "fallbackGateway must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void execute(RecommendBuddiesInputData inputData) {
        // TODO [Track D]:
        // 1. Load currentUser, candidates, events, participations, and ratings from dataAccess.
        // 2. Build BuddyRecommendationContext per candidate and call scoringStrategy.score(...).
        // 3. Filter penalized candidates if inputData.isHideDoNotMeetAgain() is true;
        //    sort by totalScore DESC, candidate.getUserId() ASC; take top maxResults.
        // 4. Identify up to MAX_SUGGESTED_EVENTS open upcoming events (event.isOpenAt(clock.instant()))
        //    matching currentUser.getInterestedCommunityIds() that currentUser has not joined.
        // 5. Call primaryLlmGateway.generateExplanations(...); on any exception or missing entries,
        //    degrade cleanly to fallbackGateway.generateExplanations(...) without failing the use case.
        // 6. Present via outputBoundary.prepareSuccessView(...).
        outputBoundary.prepareFailView(
                "TODO [Track D]: Implement RecommendBuddiesInteractor.execute");
    }
}
