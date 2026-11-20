package use_case.browse_events;

import entity.Community;
import entity.Event;
import entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Narrow Data Access Interface for browsing community events (ISP).
 */
public interface BrowseEventsDataAccessInterface {

    /**
     * Checks whether the specified community ID exists.
     *
     * @param communityId canonical community ID
     * @return {@code true} if valid
     */
    boolean communityExists(String communityId);

    /**
     * Returns all communities for name resolution.
     *
     * @return list of communities
     */
    List<Community> findAllCommunities();

    /**
     * Returns events for a specific community, or all events if {@code communityId} is null/ALL.
     *
     * @param communityId community ID or {@code "ALL"}
     * @return matching events
     */
    List<Event> findEventsByCommunity(String communityId);

    /**
     * Counts current participants for an event.
     *
     * @param eventId event ID
     * @return participant count
     */
    int countParticipants(String eventId);

    /**
     * Checks whether a user has already joined the event.
     *
     * @param userId user ID
     * @param eventId event ID
     * @return {@code true} if already participating
     */
    boolean hasUserJoined(String userId, String eventId);

    /**
     * Looks up a user by ID (for creator display name).
     *
     * @param userId user ID
     * @return matching user if present
     */
    Optional<User> findUserById(String userId);
}
