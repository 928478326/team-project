package use_case.recommend_buddies;

import entity.BuddyRating;
import entity.Event;
import entity.Participation;
import entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Narrow Data Access Interface for loading the read-only snapshot required by the
 * 3+4 buddy recommendation algorithm (ISP).
 */
public interface RecommendBuddiesDataAccessInterface {

    /**
     * Loads the active user's profile.
     *
     * @param userId active user ID
     * @return active user if found
     */
    Optional<User> findUserById(String userId);

    /**
     * Loads all other candidate users excluding {@code excludeUserId}.
     *
     * @param excludeUserId current user ID
     * @return candidate users
     */
    List<User> findAllOtherUsers(String excludeUserId);

    /**
     * Loads all events so participation community histories and upcoming suggestions can be derived.
     *
     * @return all events
     */
    List<Event> findAllEvents();

    /**
     * Loads all participation records.
     *
     * @return all participations
     */
    List<Participation> findAllParticipations();

    /**
     * Loads all buddy ratings for personal history and global candidate score aggregation.
     *
     * @return all buddy ratings
     */
    List<BuddyRating> findAllRatings();
}
