package use_case.view_rateable_buddies;

import java.time.Instant;
import java.util.List;

import use_case.view_rateable_buddies.ViewRateableBuddiesOutputData.RateableBuddyItem;

/** Read boundary for the student's shared meetups and their own previous feedback. */
public interface ViewRateableBuddiesDataAccessInterface {
    /**
     * Lists the other participants in the student's events that started at or before the supplied time.
     * @param userId current student
     * @param asOf latest eligible event start, inclusive
     * @return one item per buddy and shared event
     * @throws IllegalStateException if storage is unavailable
     */
    List<RateableBuddyItem> findRateableBuddies(String userId, Instant asOf);
}
