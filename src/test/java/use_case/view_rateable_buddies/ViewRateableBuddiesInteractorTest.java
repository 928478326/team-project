package use_case.view_rateable_buddies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import use_case.view_rateable_buddies.ViewRateableBuddiesOutputData.RateableBuddyItem;

class ViewRateableBuddiesInteractorTest implements ViewRateableBuddiesOutputBoundary {

    private static final Instant NOW = Instant.parse("2026-11-20T20:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private ViewRateableBuddiesOutputData result;
    private String error;

    @Test
    void listsPastEventsNewestFirstAndBreaksTiesByEventThenBuddy() {
        final RateableBuddyItem previous = unrated("alpha", "amy", NOW.minusSeconds(1));
        final RateableBuddyItem laterEventId = unrated("beta", "amy", NOW);
        final RateableBuddyItem laterBuddyId = unrated("alpha", "zoe", NOW);
        final RateableBuddyItem rated = new RateableBuddyItem(
                "alpha", "Study", NOW, "bob", "Bob", true, 4, true, "Fun");
        final ViewRateableBuddiesInteractor interactor = new ViewRateableBuddiesInteractor((user, cutoff) -> {
            assertEquals("alice", user);
            assertEquals(NOW, cutoff);
            return List.of(previous, laterBuddyId, laterEventId, rated);
        }, this, CLOCK);
        interactor.execute(new ViewRateableBuddiesInputData(" alice "));

        assertNull(error);
        assertEquals(List.of(rated, laterBuddyId, laterEventId, previous), result.getItems());
        final RateableBuddyItem first = result.getItems().get(0);
        assertEquals("alpha", first.getEventId());
        assertEquals("Study", first.getEventTitle());
        assertEquals(NOW, first.getEventStartsAt());
        assertEquals("bob", first.getBuddyUserId());
        assertEquals("Bob", first.getBuddyDisplayName());
        assertTrue(first.isAlreadyRated());
        assertEquals(4, first.getExistingScore());
        assertTrue(first.isExistingWouldMeetAgain());
        assertEquals("Fun", first.getExistingComment());
        assertFalse(previous.isAlreadyRated());
        assertEquals("", previous.getExistingComment());
    }

    @Test
    void presentsAnEmptyListWhenNoBuddiesAreEligible() {
        final ViewRateableBuddiesInteractor interactor = new ViewRateableBuddiesInteractor(
                (user, cutoff) -> List.of(), this);
        interactor.execute(new ViewRateableBuddiesInputData("alice"));
        assertNull(error);
        assertTrue(result.getItems().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsMissingUserBeforeReading(String user) {
        final ViewRateableBuddiesInteractor interactor = new ViewRateableBuddiesInteractor(
                (ignored, cutoff) -> fail("Unexpected lookup"), this, CLOCK);
        interactor.execute(new ViewRateableBuddiesInputData(user));
        assertNull(result);
        assertEquals("Sign in to see your meetup buddies.", error);
    }

    @Test
    void presentsFailureWhenLookupIsUnavailable() {
        final ViewRateableBuddiesInteractor interactor = new ViewRateableBuddiesInteractor((user, cutoff) -> {
            throw new IllegalStateException("read unavailable");
        }, this, CLOCK);
        interactor.execute(new ViewRateableBuddiesInputData("alice"));
        assertNull(result);
        assertEquals("Could not load your meetup buddies. Please try again.", error);
    }

    @Test
    void outputKeepsItsOwnImmutableSnapshot() {
        final RateableBuddyItem buddy = unrated("alpha", "amy", NOW);
        final List<RateableBuddyItem> source = new ArrayList<>(List.of(buddy));
        final ViewRateableBuddiesOutputData snapshot = new ViewRateableBuddiesOutputData(source);
        source.clear();
        assertEquals(List.of(buddy), snapshot.getItems());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getItems().clear());
        assertTrue(new ViewRateableBuddiesOutputData(null).getItems().isEmpty());
    }

    @Override
    public void prepareSuccessView(ViewRateableBuddiesOutputData outputData) {
        result = outputData;
    }

    @Override
    public void prepareFailView(String message) {
        error = message;
    }

    private static RateableBuddyItem unrated(String event, String buddy, Instant start) {
        return new RateableBuddyItem(event, "Study", start, buddy, buddy, false, 0, false, null);
    }
}
