package entity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Pure domain snapshot passed into {@link BuddyScoringStrategy} for scoring one candidate buddy.
 */
public final class BuddyRecommendationContext {

    private final User currentUser;
    private final User candidateUser;
    private final Set<String> currentUserEventCommunities;
    private final Set<String> candidateEventCommunities;
    private final int sharedEventCount;
    private final BuddyRating latestRatingByCurrentUser;
    private final Double candidateAverageRating;
    private final int candidateRatingCount;

    /**
     * Constructs an immutable scoring context.
     *
     * @param currentUser logged-in student (non-null)
     * @param candidateUser candidate buddy being scored (non-null)
     * @param currentUserEventCommunities community IDs of events joined by {@code currentUser}
     * @param candidateEventCommunities community IDs of events joined by {@code candidateUser}
     * @param sharedEventCount number of events both users participated in (>= 0)
     * @param latestRatingByCurrentUser latest rating from currentUser to candidate (nullable)
     * @param candidateAverageRating global mean rating received by candidate (nullable if unrated)
     * @param candidateRatingCount number of ratings received by candidate (>= 0)
     */
    public BuddyRecommendationContext(
            User currentUser,
            User candidateUser,
            Set<String> currentUserEventCommunities,
            Set<String> candidateEventCommunities,
            int sharedEventCount,
            BuddyRating latestRatingByCurrentUser,
            Double candidateAverageRating,
            int candidateRatingCount) {
        this.currentUser = Objects.requireNonNull(currentUser, "currentUser must not be null");
        this.candidateUser = Objects.requireNonNull(candidateUser, "candidateUser must not be null");
        this.currentUserEventCommunities = immutableCopy(currentUserEventCommunities);
        this.candidateEventCommunities = immutableCopy(candidateEventCommunities);
        if (sharedEventCount < 0) {
            throw new IllegalArgumentException("sharedEventCount must be non-negative.");
        }
        if (candidateRatingCount < 0) {
            throw new IllegalArgumentException("candidateRatingCount must be non-negative.");
        }
        this.sharedEventCount = sharedEventCount;
        this.latestRatingByCurrentUser = latestRatingByCurrentUser;
        this.candidateAverageRating = candidateAverageRating;
        this.candidateRatingCount = candidateRatingCount;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User getCandidateUser() {
        return candidateUser;
    }

    public Set<String> getCurrentUserEventCommunities() {
        return currentUserEventCommunities;
    }

    public Set<String> getCandidateEventCommunities() {
        return candidateEventCommunities;
    }

    public int getSharedEventCount() {
        return sharedEventCount;
    }

    public BuddyRating getLatestRatingByCurrentUser() {
        return latestRatingByCurrentUser;
    }

    public Double getCandidateAverageRating() {
        return candidateAverageRating;
    }

    public int getCandidateRatingCount() {
        return candidateRatingCount;
    }

    private static Set<String> immutableCopy(Set<String> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(source));
    }
}
