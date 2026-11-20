package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit tests verifying Enterprise Business Rules and invariants across core {@code entity} types.
 */
class EntityInvariantsTest {

    @Test
    void testWeekendStyleCompatibilityMatrixIsSymmetric() {
        for (WeekendStyle a : WeekendStyle.values()) {
            assertEquals(WeekendStyle.SAME_STYLE_SCORE, a.compatibilityWith(a));
            for (WeekendStyle b : WeekendStyle.values()) {
                assertEquals(a.compatibilityWith(b), b.compatibilityWith(a));
            }
        }
        assertEquals(
                WeekendStyle.CONTRASTING_STYLE_SCORE,
                WeekendStyle.INDOOR_CHILL.compatibilityWith(WeekendStyle.OUTDOOR_ACTIVE));
        assertEquals(WeekendStyle.STUDY_FOCUS, WeekendStyle.fromCode(" study_focus "));
    }

    @Test
    void testUserFactoryAndWithProfilePreserveIdentity() {
        UserFactory factory = new CommonUserFactory();
        User created = factory.create("u-1", "  Henry  ", "hash123");

        assertEquals("henry", created.getUsername());
        assertEquals("henry", created.getDisplayName());
        assertEquals(WeekendStyle.INDOOR_CHILL, created.getWeekendStyle());
        assertTrue(created.matchesPasswordHash("hash123"));
        assertFalse(created.matchesPasswordHash("wrong"));

        User updated =
                created.withProfile(
                        "Henry Z",
                        "CS student",
                        "discord: henry",
                        WeekendStyle.FOOD_EXPLORE,
                        Set.of("Study", "GAMING"));
        assertEquals("u-1", updated.getUserId());
        assertEquals("henry", updated.getUsername());
        assertEquals("hash123", updated.getPasswordHash());
        assertEquals("Henry Z", updated.getDisplayName());
        assertEquals(Set.of("study", "gaming"), updated.getInterestedCommunityIds());
    }

    @Test
    void testEventOpenBoundaryAndCapacityRules() {
        Instant start = Instant.parse("2026-12-03T19:00:00Z");
        Event event = new Event("e-1", "Study", "u-1", "CSC207 Review", "Review", 2, start);

        assertTrue(event.isOpenAt(start.minusSeconds(1)));
        assertFalse(event.isOpenAt(start));
        assertFalse(event.isOpenAt(start.plusSeconds(1)));

        assertTrue(event.hasRemainingSeat(0));
        assertTrue(event.hasRemainingSeat(1));
        assertFalse(event.hasRemainingSeat(2));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Event("e-2", "study", "u-1", "Invalid Cap", "", 0, start));
    }

    @Test
    void testCourseEnrollmentNormalizationAndValidation() {
        CourseEnrollment enrollment =
                new CourseEnrollment("u-1", " csc207h1f ", "lec0101", "tut0201");
        assertEquals("CSC207H1F", enrollment.getCourseCode());
        assertEquals("LEC0101", enrollment.getLecSection());
        assertEquals("TUT0201", enrollment.getTutSection());
        assertTrue(enrollment.hasTutSection());

        CourseEnrollment noTut = new CourseEnrollment("u-1", "CSC207H1", "LEC5101", "  ");
        assertFalse(noTut.hasTutSection());

        assertThrows(
                IllegalArgumentException.class,
                () -> new CourseEnrollment("u-1", "BAD123", "LEC0101", ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CourseEnrollment("u-1", "CSC207H1F", "L0101", ""));
    }

    @Test
    void testBuddyRatingPreventsSelfRatingAndEnforcesScoreBounds() {
        Instant now = Instant.parse("2026-11-20T20:00:00Z");
        BuddyRating rating = new BuddyRating("u-1", "u-2", "e-1", 5, true, "Great!", now);
        assertEquals(5, rating.getScore());
        assertTrue(rating.isWouldMeetAgain());

        assertThrows(
                IllegalArgumentException.class,
                () -> new BuddyRating("u-1", "u-1", "e-1", 5, true, "", now));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BuddyRating("u-1", "u-2", "e-1", 0, false, "", now));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BuddyRating("u-1", "u-2", "e-1", 6, true, "", now));
    }
}
