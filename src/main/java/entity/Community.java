package entity;

import java.util.List;
import java.util.Locale;

/**
 * Domain entity representing an interest community that categorizes events and user preferences.
 */
public final class Community {

    public static final String STUDY_ID = "study";
    public static final String SPORTS_ID = "sports";
    public static final String GAMING_ID = "gaming";
    public static final String FOOD_ID = "food";
    public static final String CHILL_ID = "chill";

    private static final List<Community> DEFAULT_COMMUNITIES = List.of(
            new Community(STUDY_ID, "Study"),
            new Community(SPORTS_ID, "Sports"),
            new Community(GAMING_ID, "Gaming"),
            new Community(FOOD_ID, "Food"),
            new Community(CHILL_ID, "Chill"));

    private final String communityId;
    private final String name;

    /**
     * Constructs an immutable {@code Community}.
     *
     * @param communityId canonical lowercase identifier (non-blank)
     * @param name human-readable community name (non-blank)
     */
    public Community(String communityId, String name) {
        if (communityId == null || communityId.isBlank()) {
            throw new IllegalArgumentException("communityId must not be blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("community name must not be blank.");
        }
        this.communityId = communityId.trim().toLowerCase(Locale.ROOT);
        this.name = name.trim();
    }

    /**
     * Returns the canonical list of 5 MVP communities.
     *
     * @return immutable list of default communities
     */
    public static List<Community> defaultCommunities() {
        return DEFAULT_COMMUNITIES;
    }

    public String getCommunityId() {
        return communityId;
    }

    public String getName() {
        return name;
    }
}
