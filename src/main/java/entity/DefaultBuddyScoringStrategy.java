package entity;

import java.util.Collections;

/**
 * Default 3+4 Buddy Recommendation Scoring Strategy:
 * <ul>
 *   <li>Dimension A (0..35): WeekendStyle compatibility</li>
 *   <li>Dimension B (0..35): Interested communities Jaccard (max 20)
 *       + historical event community overlap (max 10) + shared events bonus (2.5 each, max 5)</li>
 *   <li>Dimension C (0..30): Personal + global buddy rating (neutral cold-start = 15)</li>
 *   <li>Downgrade rule: if latest personal rating has {@code !wouldMeetAgain} or {@code score <= 2},
 *       multiply total score by 0.2 and flag {@code penalizedByPriorRating = true}</li>
 * </ul>
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 */
public final class DefaultBuddyScoringStrategy implements BuddyScoringStrategy {

    public static final double INTEREST_JACCARD_WEIGHT = 20.0;
    public static final double HISTORY_COMMUNITY_WEIGHT = 10.0;
    public static final double SHARED_EVENT_POINTS = 2.5;
    public static final double SHARED_EVENT_MAX_POINTS = 5.0;
    public static final double COLD_START_RATING_SCORE = 15.0;
    public static final double NEGATIVE_RATING_PENALTY_FACTOR = 0.2;
    public static final int NEGATIVE_SCORE_THRESHOLD = 2;

    @Override
    public BuddyScoreBreakdown score(BuddyRecommendationContext context) {
        // TODO [Track D]: Implement the deterministic 3+4 formula described above and
        // verify all branches (same/contrasting styles, Jaccard edge cases, shared events cap,
        // personal rating, global rating, cold start, and negative-rating penalty) in unit tests.
        double styleScore = context.getCurrentUser()
                .getWeekendStyle()
                .compatibilityWith(context.getCandidateUser().getWeekendStyle());
        return new BuddyScoreBreakdown(
                context.getCandidateUser(),
                styleScore,
                styleScore,
                0.0,
                0.0,
                Collections.emptySet(),
                context.getSharedEventCount(),
                context.getCandidateAverageRating(),
                false);
    }
}
