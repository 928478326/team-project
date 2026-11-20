package use_case.rate_buddy;

import entity.BuddyRating;

/**
 * Narrow Data Access Interface for validating shared-event participation and upserting ratings (ISP).
 */
public interface RateBuddyDataAccessInterface {

    /**
     * Checks whether both {@code raterId} and {@code rateeId} hold participation records
     * for {@code eventId}.
     *
     * @param raterId rating author user ID
     * @param rateeId target buddy user ID
     * @param eventId shared event ID
     * @return {@code true} if both users participated in {@code eventId}
     */
    boolean haveUsersSharedEvent(String raterId, String rateeId, String eventId);

    /**
     * Inserts or updates a {@link BuddyRating} keyed by {@code (raterId, rateeId, eventId)}.
     *
     * @param rating validated rating entity
     */
    void upsertRating(BuddyRating rating);
}
