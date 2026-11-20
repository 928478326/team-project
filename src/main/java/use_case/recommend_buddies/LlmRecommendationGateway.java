package use_case.recommend_buddies;

import entity.BuddyScoreBreakdown;
import entity.User;
import java.util.List;
import java.util.Map;

/**
 * External gateway abstraction for generating natural-language match reasons and icebreakers
 * for top-ranked buddies (DIP + Strategy Pattern).
 *
 * <p>Both the HTTP LLM client and the deterministic rule-based fallback implement this interface,
 * guaranteeing seamless degradation when offline or unconfigured (LSP).
 */
public interface LlmRecommendationGateway {

    /**
     * Produces explanations keyed by {@code candidate.getUserId()}.
     *
     * @param currentUser active user requesting recommendations
     * @param topCandidates top-K scored buddy breakdowns
     * @return map of candidate userId to {@link BuddyExplanation}
     */
    Map<String, BuddyExplanation> generateExplanations(
            User currentUser, List<BuddyScoreBreakdown> topCandidates);

    /**
     * Immutable explanation and icebreaker pair for one candidate buddy.
     */
    final class BuddyExplanation {

        private final String reason;
        private final String icebreaker;
        private final boolean aiGenerated;

        public BuddyExplanation(String reason, String icebreaker, boolean aiGenerated) {
            this.reason = reason == null ? "" : reason;
            this.icebreaker = icebreaker == null ? "" : icebreaker;
            this.aiGenerated = aiGenerated;
        }

        public String getReason() {
            return reason;
        }

        public String getIcebreaker() {
            return icebreaker;
        }

        public boolean isAiGenerated() {
            return aiGenerated;
        }
    }
}
