package data_access.in_memory;

import entity.Community;
import entity.Event;
import entity.Participation;
import entity.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import use_case.browse_events.BrowseEventsDataAccessInterface;
import use_case.browse_events.BrowseEventsInputData;
import use_case.create_event.CreateEventDataAccessInterface;
import use_case.join_event.JoinEventDataAccessInterface;
import use_case.view_calendar.ViewCalendarDataAccessInterface;

/**
 * In-memory event &amp; participation DAO implementing browse, create, atomic join, and calendar DAIs.
 */
public final class InMemoryEventDataAccess
        implements BrowseEventsDataAccessInterface,
                CreateEventDataAccessInterface,
                JoinEventDataAccessInterface,
                ViewCalendarDataAccessInterface {

    private static final Comparator<Event> EVENT_ORDER =
            Comparator.comparing(Event::getStartsAt).thenComparing(Event::getEventId);

    private final InMemoryCampusBuddyStore store;

    public InMemoryEventDataAccess(InMemoryCampusBuddyStore store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    @Override
    public boolean communityExists(String communityId) {
        synchronized (store) {
            return store.communitiesById().containsKey(communityId);
        }
    }

    @Override
    public List<Community> findAllCommunities() {
        synchronized (store) {
            return new ArrayList<>(store.communitiesById().values());
        }
    }

    @Override
    public List<Event> findEventsByCommunity(String communityId) {
        synchronized (store) {
            boolean all = communityId == null
                    || communityId.isBlank()
                    || BrowseEventsInputData.ALL_COMMUNITIES.equalsIgnoreCase(communityId);
            List<Event> result = new ArrayList<>();
            for (Event event : store.eventsById().values()) {
                if (all || event.getCommunityId().equalsIgnoreCase(communityId)) {
                    result.add(event);
                }
            }
            result.sort(EVENT_ORDER);
            return result;
        }
    }

    @Override
    public int countParticipants(String eventId) {
        synchronized (store) {
            int count = 0;
            for (Participation participation : store.participationsByKey().values()) {
                if (participation.getEventId().equals(eventId)) {
                    count++;
                }
            }
            return count;
        }
    }

    @Override
    public boolean hasUserJoined(String userId, String eventId) {
        synchronized (store) {
            return store.participationsByKey()
                    .containsKey(InMemoryCampusBuddyStore.participationKey(userId, eventId));
        }
    }

    @Override
    public Optional<User> findUserById(String userId) {
        synchronized (store) {
            return Optional.ofNullable(store.usersById().get(userId));
        }
    }

    @Override
    public void saveEvent(Event event) {
        synchronized (store) {
            store.putEvent(event);
        }
    }

    @Override
    public Optional<Event> findEventById(String eventId) {
        synchronized (store) {
            return Optional.ofNullable(store.eventsById().get(eventId));
        }
    }

    @Override
    public JoinAttemptResult joinAtomically(String userId, String eventId, Instant now) {
        synchronized (store) {
            Event event = store.eventsById().get(eventId);
            if (event == null) {
                return new JoinAttemptResult(Outcome.EVENT_NOT_FOUND, 0, 0, null);
            }
            String key = InMemoryCampusBuddyStore.participationKey(userId, eventId);
            Participation existing = store.participationsByKey().get(key);
            int currentCount = countParticipants(eventId);
            if (existing != null) {
                return new JoinAttemptResult(
                        Outcome.ALREADY_JOINED,
                        currentCount,
                        event.getCapacity(),
                        existing.getJoinedAt());
            }
            if (!event.isOpenAt(now)) {
                return new JoinAttemptResult(
                        Outcome.EVENT_CLOSED, currentCount, event.getCapacity(), null);
            }
            if (!event.hasRemainingSeat(currentCount)) {
                return new JoinAttemptResult(
                        Outcome.FULL, currentCount, event.getCapacity(), null);
            }
            Participation created = new Participation(userId, eventId, now);
            store.putParticipation(created);
            return new JoinAttemptResult(
                    Outcome.JOINED, currentCount + 1, event.getCapacity(), now);
        }
    }

    @Override
    public List<Event> findJoinedEventsBetween(
            String userId, Instant inclusiveStart, Instant exclusiveEnd) {
        synchronized (store) {
            List<Event> result = new ArrayList<>();
            for (Participation participation : store.participationsByKey().values()) {
                if (!participation.getUserId().equals(userId)) {
                    continue;
                }
                Event event = store.eventsById().get(participation.getEventId());
                if (event != null
                        && !event.getStartsAt().isBefore(inclusiveStart)
                        && event.getStartsAt().isBefore(exclusiveEnd)) {
                    result.add(event);
                }
            }
            result.sort(EVENT_ORDER);
            return result;
        }
    }
}
