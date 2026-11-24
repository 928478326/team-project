package use_case.rate_buddy;

import entity.BuddyRating;

/** Write boundary for post-meetup feedback. */
public interface RateBuddyDataAccessInterface {
    /**
     * Saves or replaces the author's rating for this buddy and event in one operation.
     * Both students must have joined the event and it must have started by the rating timestamp.
     * @param rating validated feedback
     * @return whether the pair was eligible and the write succeeded
     * @throws IllegalStateException if storage is unavailable
     */
    boolean saveIfEligible(BuddyRating rating);
}
