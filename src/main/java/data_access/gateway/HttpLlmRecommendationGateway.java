package data_access.gateway;

import entity.BuddyScoreBreakdown;
import entity.User;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import use_case.recommend_buddies.LlmRecommendationGateway;

/**
 * External LLM REST API adapter (e.g., Gemini or Cohere Chat API via OkHttp + org.json)
 * generating personalized buddy match explanations and icebreaker prompts.
 *
 * <p>API key MUST be read exclusively from environment variable {@value #API_KEY_ENV_VAR}
 * and NEVER hardcoded or committed to Git.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class HttpLlmRecommendationGateway implements LlmRecommendationGateway {

    /** Environment variable supplying the LLM API key at runtime. */
    public static final String API_KEY_ENV_VAR = "CAMPUSBUDDY_LLM_API_KEY";

    private final String apiKey;

    public HttpLlmRecommendationGateway() {
        this(System.getenv(API_KEY_ENV_VAR));
    }

    public HttpLlmRecommendationGateway(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    @Override
    public Map<String, BuddyExplanation> generateExplanations(
            User currentUser, List<BuddyScoreBreakdown> topCandidates) {
        // TODO [Track D]:
        // 1. If apiKey is blank or topCandidates is empty, return Collections.emptyMap()
        //    (RecommendBuddiesInteractor will seamlessly fall back to RuleBasedExplanationFallback).
        // 2. Build a single batched JSON prompt summarizing currentUser + topCandidates breakdowns.
        // 3. Call Gemini/Cohere REST API via OkHttp with a <= 5s timeout, parse JSON response
        //    into BuddyExplanation(..., aiGenerated = true).
        if (apiKey.isEmpty() || topCandidates == null || topCandidates.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.emptyMap();
    }
}
