package use_case.view_calendar;

import entity.Community;
import entity.Event;
import java.time.Instant;
import java.util.List;

/**
 * Narrow Data Access Interface for querying a user's joined events within a time window (ISP).
 */
public interface ViewCalendarDataAccessInterface {

    /**
     * Returns all events joined by {@code userId} whose {@code startsAt} falls in
     * {@code [inclusiveStart, exclusiveEnd)}, sorted by {@code startsAt ASC, eventId ASC}.
     *
     * @param userId active user ID
     * @param inclusiveStart start of the month in UTC
     * @param exclusiveEnd start of the next month in UTC
     * @return ordered list of joined events
     */
    List<Event> findJoinedEventsBetween(
            String userId, Instant inclusiveStart, Instant exclusiveEnd);

    /**
     * Returns all communities for community name resolution.
     *
     * @return list of communities
     */
    List<Community> findAllCommunities();
}
