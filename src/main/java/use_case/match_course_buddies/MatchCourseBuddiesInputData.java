package use_case.match_course_buddies;

import java.util.Locale;

/**
 * Immutable input data for matching course study buddies.
 */
public final class MatchCourseBuddiesInputData {

    /** Supported match filter modes. */
    public static final String FILTER_ALL = "ALL";
    public static final String FILTER_SAME_LEC = "SAME_LEC";
    public static final String FILTER_SAME_TUT = "SAME_TUT";

    private final String userId;
    private final String filterMode;

    public MatchCourseBuddiesInputData(String userId, String filterMode) {
        this.userId = userId == null ? "" : userId.trim();
        this.filterMode = (filterMode == null || filterMode.isBlank())
                ? FILTER_ALL
                : filterMode.trim().toUpperCase(Locale.ROOT);
    }

    public String getUserId() {
        return userId;
    }

    public String getFilterMode() {
        return filterMode;
    }
}
