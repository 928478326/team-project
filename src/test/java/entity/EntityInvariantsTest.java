package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit tests verifying Enterprise Business Rules, 3-Tier Profile Visibility invariants,
 * free-text {@link WeekendStyle} behavior, and core {@code entity} contracts.
 */
class EntityInvariantsTest {

    @Test
    void testWeekendStylePresetCompatibilityMatrixAndPersistenceHelpers() {
        assertEquals(5, WeekendStyle.presets().size());
        assertEquals(5, WeekendStyle.values().length);

        for (WeekendStyle a : WeekendStyle.values()) {
            assertTrue(a.isPreset());
            assertEquals(WeekendStyle.SAME_STYLE_SCORE, a.compatibilityWith(a));
            assertEquals(a, WeekendStyle.valueOf(a.name()));
            assertFalse(a.getDescription().isBlank());
            assertEquals(a.getDisplayName(), a.toString());
            for (WeekendStyle b : WeekendStyle.values()) {
                assertEquals(a.compatibilityWith(b), b.compatibilityWith(a));
            }
        }

        assertEquals(
                WeekendStyle.COMPATIBLE_STYLE_SCORE,
                WeekendStyle.INDOOR_CHILL.compatibilityWith(WeekendStyle.STUDY_FOCUS));
        assertEquals(
                WeekendStyle.CONTRASTING_STYLE_SCORE,
                WeekendStyle.INDOOR_CHILL.compatibilityWith(WeekendStyle.OUTDOOR_ACTIVE));
        assertEquals(
                WeekendStyle.NEUTRAL_STYLE_SCORE,
                WeekendStyle.STUDY_FOCUS.compatibilityWith(WeekendStyle.OUTDOOR_ACTIVE));
        assertEquals(WeekendStyle.STUDY_FOCUS, WeekendStyle.fromCode(" study_focus "));
        assertEquals(WeekendStyle.INDOOR_CHILL, WeekendStyle.of("Indoor Chill"));
    }

    @Test
    void testWeekendStyleSupportsCustomFreeTextAndValidatesBounds() {
        final WeekendStyle customA =
                WeekendStyle.of("  Saturday badminton at Goldring & Sunday Robarts study  ");
        final WeekendStyle customB =
                WeekendStyle.of("Badminton session on Saturday and quiet cafe study");
        final WeekendStyle customDisjoint =
                WeekendStyle.of("Late night hotpot karaoke downtown");
        final WeekendStyle shortSymbolsOnly = WeekendStyle.of("a & b !");

        assertFalse(customA.isPreset());
        assertEquals(
                "Saturday badminton at Goldring & Sunday Robarts study",
                customA.getDisplayName());
        assertEquals(customA.getDisplayName(), customA.name());
        assertEquals(customA.getDisplayName(), customA.getDescription());
        assertEquals(customA, WeekendStyle.valueOf(customA.name()));
        assertEquals(customA.hashCode(), WeekendStyle.of(customA.name()).hashCode());
        assertEquals(customA, customA);
        assertNotEquals(customA, "not-a-weekend-style");
        assertNotEquals(customA, WeekendStyle.INDOOR_CHILL);

        final int overlapScore = customA.compatibilityWith(customB);
        final int disjointScore = customA.compatibilityWith(customDisjoint);
        assertEquals(overlapScore, customB.compatibilityWith(customA));
        assertTrue(overlapScore > disjointScore);
        assertEquals(WeekendStyle.CONTRASTING_STYLE_SCORE, disjointScore);
        assertEquals(
                WeekendStyle.NEUTRAL_STYLE_SCORE,
                customA.compatibilityWith(shortSymbolsOnly));
        assertEquals(
                WeekendStyle.NEUTRAL_STYLE_SCORE,
                shortSymbolsOnly.compatibilityWith(customA));

        assertThrows(IllegalArgumentException.class, () -> WeekendStyle.of(null));
        assertThrows(IllegalArgumentException.class, () -> WeekendStyle.of("   "));
        assertThrows(
                IllegalArgumentException.class,
                () -> WeekendStyle.of("x".repeat(WeekendStyle.MAX_LENGTH + 1)));
    }

    @Test
    void testPublicProfileValidationAndEquality() {
        final Set<String> rawCommunities = new HashSet<>(Arrays.asList(" Study ", null, "  ", "GAMING"));
        final PublicProfile profileA = new PublicProfile(
                "  Henry Z  ",
                null,
                null,
                WeekendStyle.of("Weekend boardgames & coding"),
                rawCommunities);
        final PublicProfile profileB = new PublicProfile(
                "Henry Z",
                "",
                "",
                WeekendStyle.of("Weekend boardgames & coding"),
                Set.of("study", "gaming"));

        assertEquals("Henry Z", profileA.getDisplayName());
        assertEquals("", profileA.getBio());
        assertEquals("", profileA.getContactInfo());
        assertEquals(Set.of("study", "gaming"), profileA.getInterestedCommunityIds());
        assertEquals(profileA, profileA);
        assertEquals(profileA, profileB);
        assertEquals(profileA.hashCode(), profileB.hashCode());
        assertNotEquals(profileA, "other");
        assertNotEquals(
                profileA,
                new PublicProfile("Different", "", "", WeekendStyle.INDOOR_CHILL, null));

        assertThrows(
                IllegalArgumentException.class,
                () -> new PublicProfile("  ", "", "", WeekendStyle.INDOOR_CHILL, Collections.emptySet()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PublicProfile(
                        "a".repeat(PublicProfile.MAX_DISPLAY_NAME_LENGTH + 1),
                        "",
                        "",
                        WeekendStyle.INDOOR_CHILL,
                        Collections.emptySet()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PublicProfile(
                        "Henry",
                        "b".repeat(PublicProfile.MAX_BIO_LENGTH + 1),
                        "",
                        WeekendStyle.INDOOR_CHILL,
                        Collections.emptySet()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PublicProfile(
                        "Henry",
                        "",
                        "c".repeat(PublicProfile.MAX_CONTACT_LENGTH + 1),
                        WeekendStyle.INDOOR_CHILL,
                        Collections.emptySet()));
    }

    @Test
    void testPrivateProfileUtoridVerificationAndContactMasking() {
        final PrivateProfile unverified = PrivateProfile.defaultUnverified();
        assertFalse(unverified.isUoftVerified());
        assertEquals("", unverified.getUtorid());
        assertTrue(unverified.isShareContactInfo());
        assertEquals("discord: henry", unverified.maskContactInfo("  discord: henry  "));
        assertEquals("", unverified.maskContactInfo(null));
        assertEquals(unverified, new PrivateProfile("   ", true));

        final PrivateProfile verifiedHiddenContact = new PrivateProfile("  ZhouHaol07 ", false);
        assertTrue(verifiedHiddenContact.isUoftVerified());
        assertEquals("zhouhaol07", verifiedHiddenContact.getUtorid());
        assertFalse(verifiedHiddenContact.isShareContactInfo());
        assertEquals(
                PrivateProfile.HIDDEN_CONTACT_PLACEHOLDER,
                verifiedHiddenContact.maskContactInfo("discord: henry"));
        assertFalse(verifiedHiddenContact.toString().contains("zhouhaol07"));

        assertEquals(verifiedHiddenContact, verifiedHiddenContact);
        assertEquals(verifiedHiddenContact, new PrivateProfile("zhouhaol07", false));
        assertEquals(
                verifiedHiddenContact.hashCode(),
                new PrivateProfile("zhouhaol07", false).hashCode());
        assertNotEquals(verifiedHiddenContact, unverified);
        assertNotEquals(verifiedHiddenContact, "string");

        assertThrows(IllegalArgumentException.class, () -> new PrivateProfile("ab", true));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PrivateProfile("a".repeat(PrivateProfile.MAX_UTORID_LENGTH + 1), true));
        assertThrows(IllegalArgumentException.class, () -> new PrivateProfile("bad_id!", true));
    }

    @Test
    void testLlmPersonaInvariantsAndSafeToString() {
        final LlmPersona empty = LlmPersona.empty();
        assertTrue(empty.isEmpty());
        assertEquals("", empty.getSummary());
        assertEquals(empty, LlmPersona.of(null));
        assertEquals(empty, LlmPersona.of("   "));

        final String secretTrait = "Prefers quiet evening 1-on-1 study; rated punctual by peers.";
        final LlmPersona enriched = LlmPersona.of("  " + secretTrait + "  ");
        assertFalse(enriched.isEmpty());
        assertEquals(secretTrait, enriched.getSummary());
        assertFalse(enriched.toString().contains(secretTrait));
        assertEquals(enriched, enriched);
        assertEquals(enriched, LlmPersona.of(secretTrait));
        assertEquals(enriched.hashCode(), LlmPersona.of(secretTrait).hashCode());
        assertNotEquals(enriched, empty);
        assertNotEquals(enriched, "plain-string");

        assertThrows(
                IllegalArgumentException.class,
                () -> LlmPersona.of("p".repeat(LlmPersona.MAX_SUMMARY_LENGTH + 1)));
    }

    @Test
    void testUserThreeTierProfileIsolationAndLlmEntryPoint() {
        final UserFactory factory = new CommonUserFactory();
        final User created = factory.create("u-1", "  Henry  ", "hash123");

        assertEquals("henry", created.getUsername());
        assertEquals("henry", created.getDisplayName());
        assertEquals(WeekendStyle.INDOOR_CHILL, created.getWeekendStyle());
        assertFalse(created.isUoftVerified());
        assertTrue(created.getLlmPersona().isEmpty());
        assertTrue(created.matchesPasswordHash("hash123"));
        assertFalse(created.matchesPasswordHash("wrong"));
        assertFalse(created.matchesPasswordHash(null));

        // 1. LLM enriches Tier 3 hidden persona via dedicated entry point
        final LlmPersona persona =
                LlmPersona.of("Enjoys structured algorithm review and cozy boardgames.");
        final User withPersona = created.withLlmPersona(persona);
        assertEquals(persona, withPersona.getLlmPersona());

        // 2. User updates Tier 2 private profile (UTORid + hide contact) — Tier 3 persona stays intact
        final PrivateProfile privateSettings = new PrivateProfile("henryz07", false);
        final User withPrivate = withPersona.withPrivateProfile(privateSettings);
        assertTrue(withPrivate.isUoftVerified());
        assertEquals(privateSettings, withPrivate.getPrivateProfile());
        assertEquals(persona, withPrivate.getLlmPersona());

        // 3. User updates Tier 1 public profile with free-text WeekendStyle — Tier 2 & Tier 3 stay intact
        final User updatedPublic =
                withPrivate.withProfile(
                        "Henry Z",
                        "CS student",
                        "discord: henry",
                        WeekendStyle.of("Saturday badminton & Sunday coffee shop coding"),
                        Set.of("Study", "GAMING"));
        assertEquals("u-1", updatedPublic.getUserId());
        assertEquals("henry", updatedPublic.getUsername());
        assertEquals("hash123", updatedPublic.getPasswordHash());
        assertEquals("Henry Z", updatedPublic.getDisplayName());
        assertEquals("CS student", updatedPublic.getBio());
        assertEquals("discord: henry", updatedPublic.getContactInfo());
        assertEquals(
                PrivateProfile.HIDDEN_CONTACT_PLACEHOLDER,
                updatedPublic.getPublicContactInfo());
        assertTrue(updatedPublic.isUoftVerified());
        assertEquals(persona, updatedPublic.getLlmPersona());
        assertEquals(Set.of("study", "gaming"), updatedPublic.getInterestedCommunityIds());

        // 4. Combined editable profile update also preserves Tier 3 LLM persona
        final PublicProfile nextPublic = new PublicProfile(
                "Henry Zhou",
                "Updated bio",
                "ig: @henry",
                WeekendStyle.FOOD_EXPLORE,
                Set.of("food"));
        final PrivateProfile shareEnabled = new PrivateProfile("henryz07", true);
        final User combined = updatedPublic.withEditableProfile(nextPublic, shareEnabled);
        assertEquals(nextPublic, combined.getPublicProfile());
        assertEquals("ig: @henry", combined.getPublicContactInfo());
        assertEquals(persona, combined.getLlmPersona());

        assertThrows(
                IllegalArgumentException.class,
                () -> new User("", "henry", "h", nextPublic, shareEnabled, persona));
        assertThrows(
                IllegalArgumentException.class,
                () -> new User("u-1", "  ", "h", nextPublic, shareEnabled, persona));
        assertThrows(
                IllegalArgumentException.class,
                () -> new User("u-1", "henry", "  ", nextPublic, shareEnabled, persona));
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
