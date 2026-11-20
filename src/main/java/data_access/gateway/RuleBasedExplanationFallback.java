package data_access.gateway;

import entity.BuddyScoreBreakdown;
import entity.User;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import use_case.recommend_buddies.LlmRecommendationGateway;

/**
 * Deterministic rule-based fallback implementation of {@link LlmRecommendationGateway}.
 * Ensures buddy recommendations always display helpful reasons and icebreakers even when
 * {@code CAMPUSBUDDY_LLM_API_KEY} is unset or the external LLM API times out.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class RuleBasedExplanationFallback implements LlmRecommendationGateway {

    @Override
    public Map<String, BuddyExplanation> generateExplanations(
            User currentUser, List<BuddyScoreBreakdown> topCandidates) {
        // TODO [Track D]: Enhance fallback messaging to highlight the strongest scoring dimension
        // (style, shared communities, shared events, or prior rating).
        Map<String, BuddyExplanation> result = new LinkedHashMap<>();
        for (BuddyScoreBreakdown breakdown : topCandidates) {
            User candidate = breakdown.getCandidate();
            String reason = String.format(
                    Locale.ROOT,
                    "Matched at %.0f/100 based on %s weekend style compatibility and %d shared event(s).",
                    breakdown.getTotalScore(),
                    candidate.getWeekendStyle().getDisplayName(),
                    breakdown.getSharedEventCount());
            String icebreaker =
                    "Say hi to "
                            + candidate.getDisplayName()
                            + " and ask if they want to join a "
                            + candidate.getWeekendStyle().getDisplayName()
                            + " hangout this weekend!";
            result.put(candidate.getUserId(), new BuddyExplanation(reason, icebreaker, false));
        }
        return result;
    }
}
