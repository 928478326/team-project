package data_access.in_memory;

import entity.BuddyRating;
import entity.Event;
import entity.Participation;
import entity.User;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import use_case.rate_buddy.RateBuddyDataAccessInterface;
import use_case.view_rateable_buddies.ViewRateableBuddiesDataAccessInterface;

/**
 * In-memory buddy rating DAO implementing rateable-buddies query and rating upsert DAIs.
 */
public final class InMemoryRatingDataAccess
        implements ViewRateableBuddiesDataAccessInterface, RateBuddyDataAccessInterface {

    private final InMemoryCampusBuddyStore store;

    public InMemoryRatingDataAccess(InMemoryCampusBuddyStore store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    @Override
    public List<Event> findEventsJoinedByUser(String userId) {
        synchronized (store) {
            List<Event> result = new ArrayList<>();
            for (Participation participation : store.participationsByKey().values()) {
                if (participation.getUserId().equals(userId)) {
                    Event event = store.eventsById().get(participation.getEventId());
                    if (event != null) {
                        result.add(event);
                    }
                }
            }
            return result;
        }
    }

    @Override
    public List<Participation> findParticipationsByEventId(String eventId) {
        synchronized (store) {
            List<Participation> result = new ArrayList<>();
            for (Participation participation : store.participationsByKey().values()) {
                if (participation.getEventId().equals(eventId)) {
                    result.add(participation);
                }
            }
            return result;
        }
    }

    @Override
    public Map<String, User> findUsersByIds(Set<String> userIds) {
        synchronized (store) {
            Map<String, User> result = new LinkedHashMap<>();
            if (userIds == null) {
                return result;
            }
            for (String userId : userIds) {
                User user = store.usersById().get(userId);
                if (user != null) {
                    result.put(userId, user);
                }
            }
            return result;
        }
    }

    @Override
    public List<BuddyRating> findRatingsByRater(String raterId) {
        synchronized (store) {
            List<BuddyRating> result = new ArrayList<>();
            for (BuddyRating rating : store.ratingsByKey().values()) {
                if (rating.getRaterId().equals(raterId)) {
                    result.add(rating);
                }
            }
            return result;
        }
    }

    @Override
    public boolean haveUsersSharedEvent(String raterId, String rateeId, String eventId) {
        synchronized (store) {
            boolean raterJoined = store.participationsByKey()
                    .containsKey(InMemoryCampusBuddyStore.participationKey(raterId, eventId));
            boolean rateeJoined = store.participationsByKey()
                    .containsKey(InMemoryCampusBuddyStore.participationKey(rateeId, eventId));
            return raterJoined && rateeJoined;
        }
    }

    @Override
    public void upsertRating(BuddyRating rating) {
        synchronized (store) {
            store.putRating(rating);
        }
    }
}
