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

    private static final int EXPECTED_PRESET_COUNT = 5;
    private static final int LEGACY_NAME_LENGTH = 81;
    private static final int MAX_RATING_SCORE = 5;
    private static final int INVALID_HIGH_RATING = 6;

    @Test
    void testWeekendStylePresetCompatibilityMatrixAndPersistenceHelpers() {
        assertEquals(EXPECTED_PRESET_COUNT, WeekendStyle.presets().size());
        assertEquals(EXPECTED_PRESET_COUNT, WeekendStyle.values().length);

        for (WeekendStyle styleA : WeekendStyle.values()) {
            assertTrue(styleA.isPreset());
            assertEquals(WeekendStyle.SAME_STYLE_SCORE, styleA.compatibilityWith(styleA));
            assertEquals(styleA, WeekendStyle.valueOf(styleA.name()));
            assertFalse(styleA.getDescription().isBlank());
            assertEquals(styleA.getDisplayName(), styleA.toString());
            for (WeekendStyle styleB : WeekendStyle.values()) {
                assertEquals(styleA.compatibilityWith(styleB), styleB.compatibilityWith(styleA));
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
        assertEquals(customA, customA);
        assertNotEquals(customA, "not-a-weekend-style");
        assertNotEquals(customA, WeekendStyle.INDOOR_CHILL);

        final int overlapScore = customA.compatibilityWith(customB);
        final int disjointScore = customA.compatibilityWith(customDisjoint);
        assertEquals(overlapScore, customB.compatibilityWith(customA));
        assertTrue(overlapScore > disjointScore);
        assertEquals(WeekendStyle.CONTRASTING_STYLE_SCORE, disjointScore);
        assertEquals(WeekendStyle.NEUTRAL_STYLE_SCORE, customA.compatibilityWith(shortSymbolsOnly));
        assertEquals(WeekendStyle.NEUTRAL_STYLE_SCORE, shortSymbolsOnly.compatibilityWith(customA));

        assertThrows(IllegalArgumentException.class, () -> WeekendStyle.of(null));
        assertThrows(IllegalArgumentException.class, () -> WeekendStyle.of("   "));
        assertThrows(
                IllegalArgumentException.class,
                () -> WeekendStyle.of("x".repeat(WeekendStyle.MAX_LENGTH + 1)));
    }

    @Test
    void testWeekendStyleUnicodeCaseFoldingMaintainsEqualsHashCodeContract() {
        final WeekendStyle upperGreek = WeekendStyle.of("\u039f\u03a3");
        final WeekendStyle lowerGreek = WeekendStyle.of("\u03bf\u03c3");
        final WeekendStyle finalSigmaGreek = WeekendStyle.of("\u03bf\u03c2");

        assertEquals("\u039f\u03a3", upperGreek.getDisplayName());
        assertEquals(upperGreek, lowerGreek);
        assertEquals(upperGreek, finalSigmaGreek);
        assertEquals(upperGreek.hashCode(), lowerGreek.hashCode());
        assertEquals(upperGreek.hashCode(), finalSigmaGreek.hashCode());

        final Set<WeekendStyle> styleSet = new HashSet<>();
        styleSet.add(upperGreek);
        assertTrue(styleSet.contains(lowerGreek));
        assertTrue(styleSet.contains(finalSigmaGreek));
    }

    @Test
    void testPublicProfileKeepsLegacyRecordsReadableAndValidatesNewEdits() {
        final Set<String> rawCommunities =
                new HashSet<>(Arrays.asList(" Study ", null, "  ", "GAMING"));
        final String legacyLongName = "m".repeat(LEGACY_NAME_LENGTH);
        final PublicProfile hydratedLegacy = new PublicProfile(
                legacyLongName,
                "b".repeat(PublicProfile.MAX_BIO_LENGTH + 1),
                "c".repeat(PublicProfile.MAX_CONTACT_LENGTH + 1),
                WeekendStyle.INDOOR_CHILL,
                rawCommunities);
        assertEquals(legacyLongName, hydratedLegacy.getDisplayName());
        assertEquals(Set.of("study", "gaming"), hydratedLegacy.getInterestedCommunityIds());

        final PublicProfile profileA = PublicProfile.createValidated(
                "  Henry Z  ", null, null, WeekendStyle.of("Boardgames"), rawCommunities);
        final PublicProfile profileB = PublicProfile.createValidated(
                "Henry Z", "", "", WeekendStyle.of("Boardgames"), Set.of("study", "gaming"));
        assertEquals(profileA, profileA);
        assertEquals(profileA, profileB);
        assertEquals(profileA.hashCode(), profileB.hashCode());
        assertNotEquals(profileA, "other");
        assertNotEquals(profileA, hydratedLegacy);
    }

    @Test
    void testPublicProfileCreateValidatedRejectsBlankOrOverlongNewInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    PublicProfile.createValidated(
                            "  ", "", "", WeekendStyle.INDOOR_CHILL, Collections.emptySet());
                });
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    PublicProfile.createValidated(
                            "a".repeat(PublicProfile.MAX_DISPLAY_NAME_LENGTH + 1),
                            "",
                            "",
                            WeekendStyle.INDOOR_CHILL,
                            Collections.emptySet());
                });
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    PublicProfile.createValidated(
                            "Henry",
                            "b".repeat(PublicProfile.MAX_BIO_LENGTH + 1),
                            "",
                            WeekendStyle.INDOOR_CHILL,
                            Collections.emptySet());
                });
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    PublicProfile.createValidated(
                            "Henry",
                            "",
                            "c".repeat(PublicProfile.MAX_CONTACT_LENGTH + 1),
                            WeekendStyle.INDOOR_CHILL,
                            null);
                });
    }

    @Test
    void testPrivateProfileSeparatesEnteringUtoridFromTrustedVerification() {
        final PrivateProfile unverified = PrivateProfile.defaultUnverified();
        assertFalse(unverified.hasUtorid());
        assertFalse(unverified.isUoftVerified());
        assertEquals("", unverified.getUtorid());
        assertTrue(unverified.isShareContactInfo());
        assertEquals("discord: henry", unverified.maskContactInfo("  discord: henry  "));
        assertEquals("", unverified.maskContactInfo(null));
        assertThrows(IllegalArgumentException.class, unverified::markUoftVerified);

        final PrivateProfile selfEntered = new PrivateProfile("  ZhouHaol07 ", false);
        assertTrue(selfEntered.hasUtorid());
        assertFalse(selfEntered.isUoftVerified());
        assertEquals("zhouhaol07", selfEntered.getUtorid());
        assertEquals(
                PrivateProfile.HIDDEN_CONTACT_PLACEHOLDER,
                selfEntered.maskContactInfo("discord: henry"));
        assertFalse(selfEntered.toString().contains("zhouhaol07"));

        final PrivateProfile verified = selfEntered.markUoftVerified();
        assertTrue(verified.hasUtorid());
        assertTrue(verified.isUoftVerified());
        assertTrue(verified.withUserInput("ZHOUHAOL07", true).isUoftVerified());
        assertFalse(verified.withUserInput("otherid99", true).isUoftVerified());
        assertFalse(verified.withUserInput("", true).isUoftVerified());

        assertEquals(verified, verified);
        assertEquals(verified, new PrivateProfile("zhouhaol07", false, true));
        assertEquals(verified.hashCode(), new PrivateProfile("zhouhaol07", false, true).hashCode());
        assertNotEquals(verified, selfEntered);
        assertNotEquals(verified, "string");

        assertThrows(IllegalArgumentException.class, () -> new PrivateProfile("ab", true));
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new PrivateProfile(
                            "a".repeat(PrivateProfile.MAX_UTORID_LENGTH + 1), true);
                });
        assertThrows(IllegalArgumentException.class, () -> new PrivateProfile("bad_id!", true));
    }

    @Test
    void testLlmPersonaInvariantsAndSafeToString() {
        final LlmPersona empty = LlmPersona.empty();
        assertTrue(empty.isEmpty());
        assertEquals("", empty.getSummary());
        assertEquals(empty, LlmPersona.of(null));
        assertEquals(empty, LlmPersona.of("   "));
        assertEquals(empty, LlmPersona.fromStored(null));
        assertEquals(empty, LlmPersona.fromStored("  "));

        final String secretTrait = "Prefers quiet evening 1-on-1 study; rated punctual by peers.";
        final LlmPersona enriched = LlmPersona.of("  " + secretTrait + "  ");
        assertFalse(enriched.isEmpty());
        assertEquals(secretTrait, enriched.getSummary());
        assertEquals(enriched, LlmPersona.fromStored(secretTrait));
        assertFalse(enriched.toString().contains(secretTrait));
        assertEquals(enriched, enriched);
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
        assertFalse(created.hasUtorid());
        assertFalse(created.isUoftVerified());
        assertTrue(created.getLlmPersona().isEmpty());
        assertTrue(created.matchesPasswordHash("hash123"));
        assertFalse(created.matchesPasswordHash("wrong"));
        assertFalse(created.matchesPasswordHash(null));

        final LlmPersona persona =
                LlmPersona.of("Enjoys structured algorithm review and cozy boardgames.");
        final PrivateProfile privateSettings =
                new PrivateProfile("henryz07", false).markUoftVerified();
        final User withBoth = created.withLlmPersona(persona).withPrivateProfile(privateSettings);

        final User updatedPublic = withBoth.withProfile(
                "Henry Z",
                "CS student",
                "discord: henry",
                WeekendStyle.of("Saturday badminton & Sunday coffee shop coding"),
                Set.of("Study", "GAMING"));
        assertEquals("u-1", updatedPublic.getUserId());
        assertEquals("hash123", updatedPublic.getPasswordHash());
        assertEquals("CS student", updatedPublic.getBio());
        assertEquals("discord: henry", updatedPublic.getContactInfo());
        assertEquals(
                PrivateProfile.HIDDEN_CONTACT_PLACEHOLDER,
                updatedPublic.getPublicContactInfo());
        assertTrue(updatedPublic.hasUtorid());
        assertTrue(updatedPublic.isUoftVerified());
        assertEquals(persona, updatedPublic.getLlmPersona());
        assertEquals(privateSettings, updatedPublic.getPrivateProfile());
    }

    @Test
    void testUserCombinedUpdateAndIdentityValidation() {
        final User base = new CommonUserFactory().create("u-1", "henry", "hash123");
        final LlmPersona persona = LlmPersona.of("Consistent study buddy.");
        final PublicProfile nextPublic = new PublicProfile(
                "Henry Zhou", "Bio", "ig: @henry", WeekendStyle.FOOD_EXPLORE, Set.of("food"));
        final PrivateProfile shareEnabled = new PrivateProfile("henryz07", true);

        final User combined =
                base.withLlmPersona(persona).withEditableProfile(nextPublic, shareEnabled);
        assertEquals(nextPublic, combined.getPublicProfile());
        assertEquals(WeekendStyle.FOOD_EXPLORE, combined.getWeekendStyle());
        assertEquals(Set.of("food"), combined.getInterestedCommunityIds());
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
    void testEventCourseEnrollmentAndBuddyRatingInvariants() {
        final Instant start = Instant.parse("2026-12-03T19:00:00Z");
        final Event event = new Event("e-1", "Study", "u-1", "CSC207 Review", "Review", 2, start);
        assertTrue(event.isOpenAt(start.minusSeconds(1)));
        assertFalse(event.isOpenAt(start));
        assertTrue(event.hasRemainingSeat(1));
        assertFalse(event.hasRemainingSeat(2));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Event("e-2", "study", "u-1", "Invalid Cap", "", 0, start));

        final CourseEnrollment enrollment =
                new CourseEnrollment("u-1", " csc207h1f ", "lec0101", "tut0201");
        assertEquals("CSC207H1F", enrollment.getCourseCode());
        assertTrue(enrollment.hasTutSection());
        assertFalse(new CourseEnrollment("u-1", "CSC207H1", "LEC5101", "  ").hasTutSection());

        final BuddyRating rating =
                new BuddyRating("u-1", "u-2", "e-1", MAX_RATING_SCORE, true, "Great!", start);
        assertEquals(MAX_RATING_SCORE, rating.getScore());
        assertTrue(rating.isWouldMeetAgain());
        assertThrows(
                IllegalArgumentException.class,
                () -> new BuddyRating("u-1", "u-1", "e-1", MAX_RATING_SCORE, true, "", start));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BuddyRating("u-1", "u-2", "e-1", 0, false, "", start));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BuddyRating("u-1", "u-2", "e-1", INVALID_HIGH_RATING, true, "", start));
    }
}
