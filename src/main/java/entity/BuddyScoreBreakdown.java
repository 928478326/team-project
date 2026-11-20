package entity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Pure domain value object capturing the transparent 3+4 recommendation score breakdown
 * for a single candidate buddy.
 */
public final class BuddyScoreBreakdown {

    private final User candidate;
    private final double totalScore;
    private final double styleScore;
    private final double interestAndHistoryScore;
    private final double ratingScore;
    private final Set<String> sharedInterestCommunityIds;
    private final int sharedEventCount;
    private final Double candidateAverageRating;
    private final boolean penalizedByPriorRating;

    /**
     * Constructs an immutable {@code BuddyScoreBreakdown}.
     *
     * @param candidate candidate buddy user (non-null)
     * @param totalScore final score on a 0..100 scale
     * @param styleScore dimension A score (0..35)
     * @param interestAndHistoryScore dimension B score (0..35)
     * @param ratingScore dimension C score (0..30)
     * @param sharedInterestCommunityIds intersection of interested communities
     * @param sharedEventCount number of shared past/upcoming events
     * @param candidateAverageRating candidate global average rating (nullable)
     * @param penalizedByPriorRating true if downgraded due to low prior rating / not meet again
     */
    public BuddyScoreBreakdown(
            User candidate,
            double totalScore,
            double styleScore,
            double interestAndHistoryScore,
            double ratingScore,
            Set<String> sharedInterestCommunityIds,
            int sharedEventCount,
            Double candidateAverageRating,
            boolean penalizedByPriorRating) {
        this.candidate = Objects.requireNonNull(candidate, "candidate must not be null");
        this.totalScore = totalScore;
        this.styleScore = styleScore;
        this.interestAndHistoryScore = interestAndHistoryScore;
        this.ratingScore = ratingScore;
        this.sharedInterestCommunityIds = sharedInterestCommunityIds == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new LinkedHashSet<>(sharedInterestCommunityIds));
        this.sharedEventCount = sharedEventCount;
        this.candidateAverageRating = candidateAverageRating;
        this.penalizedByPriorRating = penalizedByPriorRating;
    }

    public User getCandidate() {
        return candidate;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public double getStyleScore() {
        return styleScore;
    }

    public double getInterestAndHistoryScore() {
        return interestAndHistoryScore;
    }

    public double getRatingScore() {
        return ratingScore;
    }

    public Set<String> getSharedInterestCommunityIds() {
        return sharedInterestCommunityIds;
    }

    public int getSharedEventCount() {
        return sharedEventCount;
    }

    public Double getCandidateAverageRating() {
        return candidateAverageRating;
    }

    public boolean isPenalizedByPriorRating() {
        return penalizedByPriorRating;
    }
}
