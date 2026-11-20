package use_case.create_event;

import entity.Event;

/**
 * Narrow Data Access Interface for creating an event (ISP).
 */
public interface CreateEventDataAccessInterface {

    /**
     * Verifies that the target community exists.
     *
     * @param communityId canonical community ID
     * @return {@code true} if present
     */
    boolean communityExists(String communityId);

    /**
     * Persists a newly created {@link Event}.
     * Note: by team rule, creating an event does NOT auto-insert a Participation for the creator.
     *
     * @param event validated event entity
     */
    void saveEvent(Event event);
}
