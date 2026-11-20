package interface_adapter.course_match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UI state for the Course Schedule &amp; Study Buddy Matching view.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class CourseMatchState {

    private String filterMode = "ALL";
    private List<EnrollmentRow> enrollments = Collections.emptyList();
    private List<MatchRow> matches = Collections.emptyList();
    private String statusMessage = "";
    private String errorMessage = "";

    public String getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(String filterMode) {
        this.filterMode = filterMode == null ? "ALL" : filterMode;
    }

    public List<EnrollmentRow> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<EnrollmentRow> enrollments) {
        this.enrollments = enrollments == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(enrollments));
    }

    public List<MatchRow> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchRow> matches) {
        this.matches = matches == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(matches));
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage == null ? "" : statusMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? "" : errorMessage;
    }

    /**
     * Formatted row representing one enrolled course.
     */
    public static final class EnrollmentRow {

        private final String courseCode;
        private final String courseTitle;
        private final String lecSection;
        private final String tutSection;

        public EnrollmentRow(
                String courseCode, String courseTitle, String lecSection, String tutSection) {
            this.courseCode = courseCode;
            this.courseTitle = courseTitle;
            this.lecSection = lecSection;
            this.tutSection = tutSection;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public String getLecSection() {
            return lecSection;
        }

        public String getTutSection() {
            return tutSection;
        }
    }

    /**
     * Formatted row representing one matched study buddy.
     */
    public static final class MatchRow {

        private final String userId;
        private final String displayName;
        private final String weekendStyleLabel;
        private final String contactInfo;
        private final int matchScore;
        private final List<String> overlapSummaries;

        public MatchRow(
                String userId,
                String displayName,
                String weekendStyleLabel,
                String contactInfo,
                int matchScore,
                List<String> overlapSummaries) {
            this.userId = userId;
            this.displayName = displayName;
            this.weekendStyleLabel = weekendStyleLabel;
            this.contactInfo = contactInfo;
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
