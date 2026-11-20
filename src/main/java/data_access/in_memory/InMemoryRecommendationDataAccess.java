package data_access.in_memory;

import entity.BuddyRating;
import entity.Event;
import entity.Participation;
import entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import use_case.recommend_buddies.RecommendBuddiesDataAccessInterface;

/**
 * In-memory snapshot DAO for the 3+4 buddy recommendation engine.
 */
public final class InMemoryRecommendationDataAccess
        implements RecommendBuddiesDataAccessInterface {

    private final InMemoryCampusBuddyStore store;

    public InMemoryRecommendationDataAccess(InMemoryCampusBuddyStore store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    @Override
    public Optional<User> findUserById(String userId) {
        synchronized (store) {
            return Optional.ofNullable(store.usersById().get(userId));
        }
    }

    @Override
    public List<User> findAllOtherUsers(String excludeUserId) {
        synchronized (store) {
            List<User> result = new ArrayList<>();
            for (User user : store.usersById().values()) {
                if (!user.getUserId().equals(excludeUserId)) {
                    result.add(user);
                }
            }
            return result;
        }
    }

    @Override
    public List<Event> findAllEvents() {
        synchronized (store) {
            return new ArrayList<>(store.eventsById().values());
        }
    }

    @Override
    public List<Participation> findAllParticipations() {
        synchronized (store) {
            return new ArrayList<>(store.participationsByKey().values());
        }
    }

    @Override
    public List<BuddyRating> findAllRatings() {
        synchronized (store) {
            return new ArrayList<>(store.ratingsByKey().values());
        }
    }
}
