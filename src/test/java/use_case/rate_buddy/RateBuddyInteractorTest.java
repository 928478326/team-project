package use_case.rate_buddy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import entity.BuddyRating;

class RateBuddyInteractorTest implements RateBuddyOutputBoundary {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-20T20:00:00Z"), ZoneOffset.UTC);
    private RateBuddyOutputData confirmation;
    private String error;

    @ParameterizedTest
    @CsvSource({"1,false", "5,true"})
    void savesValidatedRatingAndPresentsItsDetails(int score, boolean meetAgain) {
        final List<BuddyRating> saved = new ArrayList<>();
        final String comment = "x".repeat(300);
        final RateBuddyInteractor interactor = new RateBuddyInteractor(saved::add, this, CLOCK);
        interactor.execute(new RateBuddyInputData(
                " alice ", " bob ", " study ", score, meetAgain, " " + comment + " "));

        assertEquals(1, saved.size());
        final BuddyRating rating = saved.get(0);
        assertEquals("alice", rating.getRaterId());
        assertEquals("bob", rating.getRateeId());
        assertEquals("study", rating.getEventId());
        assertEquals(score, rating.getScore());
        assertEquals(meetAgain, rating.isWouldMeetAgain());
        assertEquals(comment, rating.getComment());
        assertEquals(CLOCK.instant(), rating.getRatedAt());
        assertNull(error);
        assertEquals("bob", confirmation.getRateeId());
        assertEquals("study", confirmation.getEventId());
        assertEquals(score, confirmation.getScore());
        assertEquals(meetAgain, confirmation.isWouldMeetAgain());
        assertEquals("Rating saved.", confirmation.getMessage());
    }

    @ParameterizedTest
    @CsvSource({",bob,study,1,raterId must not be blank.", "alice,,study,1,rateeId must not be blank.",
                "alice,bob,' ',1,eventId must not be blank.", "alice,alice,study,1,Users cannot rate themselves.",
                "alice,bob,study,0,Rating score must be between 1 and 5.",
                "alice,bob,study,6,Rating score must be between 1 and 5."})
    void rejectsInvalidIdentityOrScore(String rater, String ratee, String event, int score, String message) {
        assertRejectedBeforeSave(new RateBuddyInputData(rater, ratee, event, score, false, null), message);
    }

    @Test
    void rejectsLongCommentsBeforeSaving() {
        assertRejectedBeforeSave(new RateBuddyInputData("alice", "bob", "study", 1, false, "x".repeat(301)),
                "Comment must not exceed 300 characters.");
    }

    @Test
    void presentsFailureWhenThePairIsIneligible() {
        final List<BuddyRating> attempts = new ArrayList<>();
        final RateBuddyInteractor interactor = new RateBuddyInteractor(rating -> {
            attempts.add(rating);
            return false;
        }, this, CLOCK);
        interactor.execute(new RateBuddyInputData("alice", "bob", "study", 1, false, null));
        assertEquals(1, attempts.size());
        assertEquals("", attempts.get(0).getComment());
        assertNull(confirmation);
        assertEquals("You can only rate someone from a shared event that has started.", error);
    }

    @Test
    void presentsFailureWhenStorageIsUnavailable() {
        final RateBuddyInteractor interactor = new RateBuddyInteractor(rating -> {
            throw new IllegalStateException("write unavailable");
        }, this, CLOCK);
        interactor.execute(new RateBuddyInputData("alice", "bob", "study", 1, true, ""));
        assertNull(confirmation);
        assertEquals("Could not save your rating. Please try again.", error);
    }

    @Override
    public void prepareSuccessView(RateBuddyOutputData outputData) {
        confirmation = outputData;
    }

    @Override
    public void prepareFailView(String message) {
        error = message;
    }

    private void assertRejectedBeforeSave(RateBuddyInputData input, String message) {
        final RateBuddyInteractor interactor = new RateBuddyInteractor(rating -> fail("Unexpected write"), this, CLOCK);
        interactor.execute(input);
        assertNull(confirmation);
        assertEquals(message, error);
    }
}
