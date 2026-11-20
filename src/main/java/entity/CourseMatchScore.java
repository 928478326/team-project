package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pure domain value object representing the course-overlap evaluation between the current user
 * and a candidate study buddy.
 */
public final class CourseMatchScore {

    private final User candidate;
    private final int totalScore;
    private final List<MatchedCourse> matchedCourses;

    /**
     * Constructs a {@code CourseMatchScore}.
     *
     * @param candidate candidate peer user (non-null)
     * @param totalScore deterministic overlap score (>= 0)
     * @param matchedCourses list of overlapping course details
     */
    public CourseMatchScore(User candidate, int totalScore, List<MatchedCourse> matchedCourses) {
        this.candidate = Objects.requireNonNull(candidate, "candidate must not be null");
        if (totalScore < 0) {
            throw new IllegalArgumentException("totalScore must be non-negative.");
        }
        this.totalScore = totalScore;
        this.matchedCourses = matchedCourses == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(matchedCourses));
    }

    public User getCandidate() {
        return candidate;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public List<MatchedCourse> getMatchedCourses() {
        return matchedCourses;
    }

    /**
     * Returns whether at least one overlapping course matches on lecture section.
     *
     * @return {@code true} if any matched course shares the same LEC
     */
    public boolean hasSameLec() {
        for (MatchedCourse course : matchedCourses) {
            if (course.isSameLec()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether at least one overlapping course matches on tutorial section.
     *
     * @return {@code true} if any matched course shares the same TUT
     */
    public boolean hasSameTut() {
        for (MatchedCourse course : matchedCourses) {
            if (course.isSameTut()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Immutable overlap record for a single shared course.
     */
    public static final class MatchedCourse {

        private final String courseCode;
        private final String lecSection;
        private final String tutSection;
        private final boolean sameLec;
        private final boolean sameTut;
        private final int points;

        public MatchedCourse(
                String courseCode,
                String lecSection,
                String tutSection,
                boolean sameLec,
                boolean sameTut,
                int points) {
            this.courseCode = Objects.requireNonNull(courseCode, "courseCode must not be null");
            this.lecSection = lecSection == null ? "" : lecSection;
            this.tutSection = tutSection == null ? "" : tutSection;
            this.sameLec = sameLec;
            this.sameTut = sameTut;
            this.points = points;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getLecSection() {
            return lecSection;
        }

        public String getTutSection() {
            return tutSection;
        }

        public boolean isSameLec() {
            return sameLec;
        }

        public boolean isSameTut() {
            return sameTut;
        }

        public int getPoints() {
            return points;
        }
    }
}
