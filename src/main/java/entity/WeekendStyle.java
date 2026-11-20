package entity;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents a student's preferred weekend hangout style.
 * Encapsulates deterministic pairwise compatibility scoring (0 to 35 points)
 * used by buddy recommendation policies.
 */
public enum WeekendStyle {
    INDOOR_CHILL("Indoor Chill", "Board games, movies, cozy hangouts"),
    STUDY_FOCUS("Study Focus", "Libraries, cafes, focused study sessions"),
    OUTDOOR_ACTIVE("Outdoor Active", "Sports, hiking, gym, campus athletics"),
    FOOD_EXPLORE("Food Explore", "Trying restaurants, bubble tea, night markets"),
    SOCIAL_EVENTS("Social Events", "Parties, campus clubs, group mixers");

    /** Score awarded when two users share the exact same weekend style. */
    public static final int SAME_STYLE_SCORE = 35;
    /** Score awarded when two weekend styles complement each other closely. */
    public static final int COMPATIBLE_STYLE_SCORE = 20;
    /** Score awarded when two weekend styles are moderately compatible. */
    public static final int NEUTRAL_STYLE_SCORE = 10;
    /** Score awarded when two weekend styles contrast strongly. */
    public static final int CONTRASTING_STYLE_SCORE = 5;

    private final String displayName;
    private final String description;

    WeekendStyle(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Returns the human-readable label for this style.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a short description of this style.
     *
     * @return description text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Computes a symmetric compatibility score between this style and {@code other}.
     *
     * @param other another student's weekend style (must not be null)
     * @return deterministic compatibility score in {5, 10, 20, 35}
     */
    public int compatibilityWith(WeekendStyle other) {
        Objects.requireNonNull(other, "other weekend style must not be null");
        if (this == other) {
            return SAME_STYLE_SCORE;
        }
        if (isCompatiblePair(this, other)) {
            return COMPATIBLE_STYLE_SCORE;
        }
        if (isContrastingPair(this, other)) {
            return CONTRASTING_STYLE_SCORE;
        }
        return NEUTRAL_STYLE_SCORE;
    }

    /**
     * Parses a weekend style enum code case-insensitively.
     *
     * @param raw enum name string
     * @return matching {@link WeekendStyle}
     * @throws IllegalArgumentException if {@code raw} is null, blank, or unknown
     */
    public static WeekendStyle fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Weekend style code cannot be blank.");
        }
        return WeekendStyle.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private static boolean isCompatiblePair(WeekendStyle a, WeekendStyle b) {
        return matchesPair(a, b, INDOOR_CHILL, STUDY_FOCUS)
                || matchesPair(a, b, INDOOR_CHILL, FOOD_EXPLORE)
                || matchesPair(a, b, OUTDOOR_ACTIVE, SOCIAL_EVENTS)
                || matchesPair(a, b, FOOD_EXPLORE, SOCIAL_EVENTS)
                || matchesPair(a, b, OUTDOOR_ACTIVE, FOOD_EXPLORE);
    }

    private static boolean isContrastingPair(WeekendStyle a, WeekendStyle b) {
        return matchesPair(a, b, STUDY_FOCUS, SOCIAL_EVENTS)
                || matchesPair(a, b, INDOOR_CHILL, OUTDOOR_ACTIVE);
    }

    private static boolean matchesPair(
            WeekendStyle a, WeekendStyle b, WeekendStyle first, WeekendStyle second) {
        return (a == first && b == second) || (a == second && b == first);
    }
}
