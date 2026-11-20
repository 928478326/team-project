package use_case.match_course_buddies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable output data for study-buddy course matches.
 */
public final class MatchCourseBuddiesOutputData {

    private final String filterMode;
    private final List<MatchItem> matches;
    private final String infoMessage;

    public MatchCourseBuddiesOutputData(
            String filterMode, List<MatchItem> matches, String infoMessage) {
        this.filterMode = filterMode;
        this.matches = matches == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(matches));
        this.infoMessage = infoMessage == null ? "" : infoMessage;
    }

    public String getFilterMode() {
        return filterMode;
    }

    public List<MatchItem> getMatches() {
        return matches;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    /**
     * Immutable DTO describing one matched peer.
     */
    public static final class MatchItem {

        private final String userId;
        private final String displayName;
        private final String weekendStyleLabel;
        private final String contactInfo;
        private final int matchScore;
        private final List<String> overlapSummaries;

        public MatchItem(
                String userId,
                String displayName,
                String weekendStyleLabel,
                String contactInfo,
                int matchScore,
                List<String> overlapSummaries) {
            this.userId = userId;
            this.displayName = displayName;
            this.weekendStyleLabel = weekendStyleLabel;
            this.contactInfo = contactInfo == null ? "" : contactInfo;
            this.matchScore = matchScore;
            this.overlapSummaries = overlapSummaries == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(overlapSummaries));
        }

        public String getUserId() {
            return userId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getWeekendStyleLabel() {
            return weekendStyleLabel;
        }

        public String getContactInfo() {
            return contactInfo;
        }

        public int getMatchScore() {
            return matchScore;
        }

        public List<String> getOverlapSummaries() {
            return overlapSummaries;
        }
    }
}
