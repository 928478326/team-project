package entity;

/**
 * Strategy interface for computing a deterministic 0..100 compatibility score
 * between two students using profile, interest/event history, and rating signals.
 */
public interface BuddyScoringStrategy {

    /**
     * Scores the candidate described in {@code context}.
     *
     * @param context pure domain snapshot for one candidate
     * @return transparent {@link BuddyScoreBreakdown}
     */
    BuddyScoreBreakdown score(BuddyRecommendationContext context);
}
