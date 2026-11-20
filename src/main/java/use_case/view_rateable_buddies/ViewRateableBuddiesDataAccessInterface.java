package use_case.view_rateable_buddies;

import entity.BuddyRating;
import entity.Event;
import entity.Participation;
import entity.User;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Narrow Data Access Interface for finding buddies who shared events with the current user (ISP).
 */
public interface ViewRateableBuddiesDataAccessInterface {

    /**
     * Returns all events joined by {@code userId}.
     *
     * @param userId active user ID
     * @return joined events
     */
    List<Event> findEventsJoinedByUser(String userId);

    /**
     * Returns all participation records for {@code eventId}.
     *
     * @param eventId shared event ID
     * @return participations in that event
     */
    List<Participation> findParticipationsByEventId(String eventId);

    /**
     * Batch-fetches user profiles by ID.
     *
     * @param userIds co-participant user IDs
     * @return map of userId to {@link User}
     */
    Map<String, User> findUsersByIds(Set<String> userIds);

    /**
     * Returns all existing ratings authored by {@code raterId}.
     *
     * @param raterId active user ID
     * @return authored ratings
     */
    List<BuddyRating> findRatingsByRater(String raterId);
}
