package entity;

import java.util.Collections;
import java.util.List;

/**
 * Default deterministic study-buddy matching strategy:
 * <ul>
 *   <li>Same course code: +10 points</li>
 *   <li>Same course code + same lecture section: additional +20 points</li>
 *   <li>Same course code + same non-empty tutorial section: additional +30 points</li>
 * </ul>
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class DefaultCourseMatchStrategy implements CourseMatchStrategy {

    public static final int SAME_COURSE_POINTS = 10;
    public static final int SAME_LEC_BONUS = 20;
    public static final int SAME_TUT_BONUS = 30;

    @Override
    public CourseMatchScore evaluate(
            List<CourseEnrollment> myEnrollments,
            User candidate,
            List<CourseEnrollment> candidateEnrollments) {
        // TODO [Track C]: Index myEnrollments by courseCode, iterate candidateEnrollments,
        // compute SAME_COURSE_POINTS / SAME_LEC_BONUS / SAME_TUT_BONUS per overlapping course,
        // sort matchedCourses deterministically by courseCode ASC, and return CourseMatchScore.
        return new CourseMatchScore(candidate, 0, Collections.emptyList());
    }
}
