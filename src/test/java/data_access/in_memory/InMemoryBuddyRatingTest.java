package data_access.in_memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import entity.BuddyRating;
import use_case.view_rateable_buddies.ViewRateableBuddiesOutputData.RateableBuddyItem;

class InMemoryBuddyRatingTest {
    private static final String GRACIE = "u-gracie";
    private static final String HENRY = "u-henry";
    private static final String EVENT = "e-midterm-hotpot";
    private final InMemoryCampusBuddyStore store = InMemoryCampusBuddyStore.createSeeded(password -> "demo-hash");
    private final InMemoryRatingDataAccess ratings = new InMemoryRatingDataAccess(store);
    private final Instant start = store.eventsById().get(EVENT).getStartsAt();

    @Test
    void sharedEventBecomesRateableAtItsStart() {
        final Instant before = start.minusMillis(1);
        assertTrue(ratings.findRateableBuddies(GRACIE, before).isEmpty());
        assertFalse(ratings.saveIfEligible(new BuddyRating(GRACIE, HENRY, EVENT, 4, true, "", before)));
        assertFalse(ratings.saveIfEligible(new BuddyRating(GRACIE, "u-james", EVENT, 4, true, "", start)));
        final List<RateableBuddyItem> peers = ratings.findRateableBuddies(GRACIE, start);
        assertEquals(Set.of(HENRY, "u-alex"),
                peers.stream().map(RateableBuddyItem::getBuddyUserId).collect(Collectors.toSet()));
        assertTrue(peers.stream().noneMatch(RateableBuddyItem::isAlreadyRated));
    }

    @Test
    void recommendationReaderSeesTheLatestRatingAndTheOtherAuthorsOriginalRating() {
        final BuddyRating original = new BuddyRating(GRACIE, HENRY, EVENT, 2, false, "", start);
        final BuddyRating edited = new BuddyRating(GRACIE, HENRY, EVENT, 5, true, "Great study partner", start);
        assertTrue(ratings.saveIfEligible(original));
        assertTrue(ratings.saveIfEligible(edited));
        final List<BuddyRating> feedback = new InMemoryRecommendationDataAccess(store).findAllRatings();
        assertEquals(2, feedback.size());
        assertTrue(feedback.contains(edited));
        assertTrue(feedback.stream().anyMatch(rating -> HENRY.equals(rating.getRaterId())));
        final RateableBuddyItem row = ratings.findRateableBuddies(GRACIE, start).stream()
                .filter(peer -> HENRY.equals(peer.getBuddyUserId())).findFirst().orElseThrow();
        assertEquals(edited.getScore(), row.getExistingScore());
        assertEquals(edited.getComment(), row.getExistingComment());
        assertTrue(row.isExistingWouldMeetAgain());
    }
}
