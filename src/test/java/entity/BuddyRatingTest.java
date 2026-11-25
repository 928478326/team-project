package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class BuddyRatingTest {

    @Test
    void trimsIdentityFieldsAndAllowsMissingComment() {
        final BuddyRating rating = new BuddyRating(" alice ", " bob ", " study ", 1, false, null, Instant.EPOCH);
        assertEquals("alice", rating.getRaterId());
        assertEquals("bob", rating.getRateeId());
        assertEquals("study", rating.getEventId());
        assertEquals("", rating.getComment());
    }

    @Test
    void requiresIdentifiedParticipantsAndTimestamp() {
        assertThrows(IllegalArgumentException.class,
                () -> new BuddyRating(null, "bob", "study", 1, false, "", Instant.EPOCH));
        assertThrows(IllegalArgumentException.class,
                () -> new BuddyRating("alice", " alice ", "study", 1, false, "", Instant.EPOCH));
        assertThrows(NullPointerException.class,
                () -> new BuddyRating("alice", "bob", "study", 1, false, "", null));
    }
}
