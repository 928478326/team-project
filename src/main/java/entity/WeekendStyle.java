package entity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable free-text Value Object representing a student's preferred weekend hangout style.
 *
 * <p>Replaces a rigid enum so students (and future LLM profile enrichment) can express nuanced
 * weekend preferences in natural language while retaining preset constants and persistence
 * helpers ({@link #name()}, {@link #valueOf(String)}) for seamless cross-track compatibility.
 */
public final class WeekendStyle {

    /** Score awarded when two users share an identical weekend style. */
    public static final int SAME_STYLE_SCORE = 35;

    /** Score awarded when two preset weekend styles complement each other closely. */
    public static final int COMPATIBLE_STYLE_SCORE = 20;

    /** Score awarded when two weekend styles are moderately compatible. */
    public static final int NEUTRAL_STYLE_SCORE = 10;

    /** Score awarded when two weekend styles have minimal overlap or contrast strongly. */
    public static final int CONTRASTING_STYLE_SCORE = 5;

    /** Maximum allowed character length for a free-text weekend style description. */
    public static final int MAX_LENGTH = 200;

    /** Minimum token length considered during local fallback text similarity scoring. */
    private static final int MIN_TOKEN_LENGTH = 3;

    private static final Pattern WORD_PATTERN = Pattern.compile("[a-z0-9]+");

    private static final String CODE_INDOOR_CHILL = "INDOOR_CHILL";
    private static final String CODE_STUDY_FOCUS = "STUDY_FOCUS";
    private static final String CODE_OUTDOOR_ACTIVE = "OUTDOOR_ACTIVE";
    private static final String CODE_FOOD_EXPLORE = "FOOD_EXPLORE";
    private static final String CODE_SOCIAL_EVENTS = "SOCIAL_EVENTS";

    /** Preset style: board games, movies, cozy hangouts. */
    public static final WeekendStyle INDOOR_CHILL = new WeekendStyle(
            CODE_INDOOR_CHILL, "Indoor Chill", "Board games, movies, cozy hangouts");

    /** Preset style: libraries, cafes, focused study sessions. */
    public static final WeekendStyle STUDY_FOCUS = new WeekendStyle(
            CODE_STUDY_FOCUS, "Study Focus", "Libraries, cafes, focused study sessions");

    /** Preset style: sports, hiking, gym, campus athletics. */
    public static final WeekendStyle OUTDOOR_ACTIVE = new WeekendStyle(
            CODE_OUTDOOR_ACTIVE, "Outdoor Active", "Sports, hiking, gym, campus athletics");

    /** Preset style: trying restaurants, bubble tea, night markets. */
    public static final WeekendStyle FOOD_EXPLORE = new WeekendStyle(
            CODE_FOOD_EXPLORE, "Food Explore", "Trying restaurants, bubble tea, night markets");

    /** Preset style: parties, campus clubs, group mixers. */
    public static final WeekendStyle SOCIAL_EVENTS = new WeekendStyle(
            CODE_SOCIAL_EVENTS, "Social Events", "Parties, campus clubs, group mixers");

    private static final List<WeekendStyle> PRESET_LIST = List.of(
            INDOOR_CHILL, STUDY_FOCUS, OUTDOOR_ACTIVE, FOOD_EXPLORE, SOCIAL_EVENTS);

    private static final Map<String, WeekendStyle> PRESETS_BY_KEY = buildPresetLookup();

    private final String presetCode;
    private final String displayName;
    private final String description;

    private WeekendStyle(String presetCode, String displayName, String description) {
        this.presetCode = presetCode;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Creates or resolves a {@code WeekendStyle} from either a preset code/label or arbitrary
     * user-written natural-language text.
     *
     * @param rawText preset identifier or custom free-text weekend preference (1..200 chars)
     * @return canonical preset instance if matched, otherwise a custom free-text instance
     * @throws IllegalArgumentException if {@code rawText} is null, blank, or exceeds {@value #MAX_LENGTH} chars
     */
    public static WeekendStyle of(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("Weekend style must not be blank.");
        }
        final String trimmed = rawText.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Weekend style must not exceed " + MAX_LENGTH + " characters.");
        }
        final WeekendStyle matchedPreset = PRESETS_BY_KEY.get(trimmed.toUpperCase(Locale.ROOT));
        return matchedPreset != null ? matchedPreset : new WeekendStyle(null, trimmed, trimmed);
    }

    /**
     * Parses a weekend style string (preset code or free-text preference).
     *
     * @param raw raw style code or free-text description
     * @return resolved {@link WeekendStyle}
     * @throws IllegalArgumentException if {@code raw} is null, blank, or too long
     */
    public static WeekendStyle fromCode(String raw) {
        return of(raw);
    }

    /**
     * Persistence factory compatible with {@code Enum.valueOf} callers (e.g., SQLite DAO).
     *
     * @param raw stored preset code or custom free-text preference
     * @return resolved {@link WeekendStyle}
     * @throws IllegalArgumentException if {@code raw} is null, blank, or too long
     */
    public static WeekendStyle valueOf(String raw) {
        return of(raw);
    }

    /**
     * Returns the five built-in starter preset styles as an array.
     *
     * @return defensive array of starter preset styles
     */
    public static WeekendStyle[] values() {
        return PRESET_LIST.toArray(new WeekendStyle[0]);
    }

    /**
     * Returns an unmodifiable list of starter preset styles for optional UI quick-picks.
     *
     * @return unmodifiable preset list
     */
    public static List<WeekendStyle> presets() {
        return PRESET_LIST;
    }

    /**
     * Returns the canonical persistence value: the uppercase preset code for presets,
     * or the exact trimmed text for custom free-text preferences.
     *
     * @return canonical serialized text
     */
    public String name() {
        return presetCode != null ? presetCode : displayName;
    }

    /**
     * Returns the human-readable weekend style text shown on public profiles.
     *
     * @return display text
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns descriptive details for presets, or the custom text itself for free-text styles.
     *
     * @return description text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Indicates whether this style is one of the built-in presets rather than custom free text.
     *
     * @return {@code true} if built-in preset
     */
    public boolean isPreset() {
        return presetCode != null;
    }

    /**
     * Computes a symmetric baseline compatibility score in {@code [5, 35]} when an external LLM
     * scorer is unavailable.
     *
     * @param other another student's weekend style (non-null)
     * @return deterministic compatibility score between {@value #CONTRASTING_STYLE_SCORE}
     *     and {@value #SAME_STYLE_SCORE}
     */
    public int compatibilityWith(WeekendStyle other) {
        Objects.requireNonNull(other, "other weekend style must not be null");
        final int score;
        if (this.equals(other)) {
            score = SAME_STYLE_SCORE;
        }
        else if (this.isPreset() && other.isPreset()) {
            score = scorePresetPair(this.presetCode, other.presetCode);
        }
        else {
            score = scoreTokenSimilarity(this.extractTokens(), other.extractTokens());
        }
        return score;
    }

    @Override
    public boolean equals(Object obj) {
        final boolean equal;
        if (this == obj) {
            equal = true;
        }
        else if (!(obj instanceof WeekendStyle)) {
            equal = false;
        }
        else {
            final WeekendStyle that = (WeekendStyle) obj;
            equal = this.isPreset() == that.isPreset()
                    && this.name().equalsIgnoreCase(that.name());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isPreset(), name().toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return displayName;
    }

    private Set<String> extractTokens() {
        final String combined = (displayName + " " + description).toLowerCase(Locale.ROOT);
        final Matcher matcher = WORD_PATTERN.matcher(combined);
        final Set<String> tokens = new LinkedHashSet<>();
        while (matcher.find()) {
            final String word = matcher.group();
            if (word.length() >= MIN_TOKEN_LENGTH) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    private static int scoreTokenSimilarity(Set<String> tokensA, Set<String> tokensB) {
        final int score;
        if (tokensA.isEmpty() || tokensB.isEmpty()) {
            score = NEUTRAL_STYLE_SCORE;
        }
        else {
            int intersection = 0;
            for (String token : tokensA) {
                if (tokensB.contains(token)) {
                    intersection++;
                }
            }
            final int union = tokensA.size() + tokensB.size() - intersection;
            final double jaccard = (double) intersection / union;
            final int range = SAME_STYLE_SCORE - CONTRASTING_STYLE_SCORE;
            score = CONTRASTING_STYLE_SCORE + (int) Math.round(jaccard * range);
        }
        return score;
    }

    private static int scorePresetPair(String codeA, String codeB) {
        final int score;
        if (isCompatiblePair(codeA, codeB)) {
            score = COMPATIBLE_STYLE_SCORE;
        }
        else if (isContrastingPair(codeA, codeB)) {
            score = CONTRASTING_STYLE_SCORE;
        }
        else {
            score = NEUTRAL_STYLE_SCORE;
        }
        return score;
    }

    private static boolean isCompatiblePair(String a, String b) {
        return matchesPair(a, b, CODE_INDOOR_CHILL, CODE_STUDY_FOCUS)
                || matchesPair(a, b, CODE_INDOOR_CHILL, CODE_FOOD_EXPLORE)
                || matchesPair(a, b, CODE_OUTDOOR_ACTIVE, CODE_SOCIAL_EVENTS)
                || matchesPair(a, b, CODE_FOOD_EXPLORE, CODE_SOCIAL_EVENTS)
                || matchesPair(a, b, CODE_OUTDOOR_ACTIVE, CODE_FOOD_EXPLORE);
    }

    private static boolean isContrastingPair(String a, String b) {
        return matchesPair(a, b, CODE_STUDY_FOCUS, CODE_SOCIAL_EVENTS)
                || matchesPair(a, b, CODE_INDOOR_CHILL, CODE_OUTDOOR_ACTIVE);
    }

    private static boolean matchesPair(String a, String b, String first, String second) {
        return (a.equals(first) && b.equals(second)) || (a.equals(second) && b.equals(first));
    }

    private static Map<String, WeekendStyle> buildPresetLookup() {
        final Map<String, WeekendStyle> map = new LinkedHashMap<>();
        for (WeekendStyle preset : PRESET_LIST) {
            map.put(preset.presetCode, preset);
            map.put(preset.displayName.toUpperCase(Locale.ROOT), preset);
        }
        return Collections.unmodifiableMap(map);
    }
}
