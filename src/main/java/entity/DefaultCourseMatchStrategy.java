package entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default deterministic study-buddy matching strategy.
 *
 * Same course: +10 points.
 * Same lecture: additional +20 points.
 * Same non-empty tutorial: additional +30 points.
 */
public final class DefaultCourseMatchStrategy
    implements CourseMatchStrategy {

    public static final int SAME_COURSE_POINTS = 10;
    public static final int SAME_LEC_BONUS = 20;
    public static final int SAME_TUT_BONUS = 30;

    @Override
    public CourseMatchScore evaluate(
        List<CourseEnrollment> myEnrollments,
        User candidate,
        List<CourseEnrollment> candidateEnrollments) {

        Map<String, CourseEnrollment> myCourses = new HashMap<>();

        for (CourseEnrollment enrollment : myEnrollments) {
            myCourses.put(enrollment.getCourseCode(), enrollment);
        }

        List<CourseMatchScore.MatchedCourse> matchedCourses =
            new ArrayList<>();

        int totalScore = 0;

        for (CourseEnrollment other : candidateEnrollments) {

            CourseEnrollment mine =
                myCourses.get(other.getCourseCode());

            if (mine == null) {
                continue;
            }

            boolean sameLec = mine.getLecSection()
                .equals(other.getLecSection());

            boolean sameTut = mine.hasTutSection()
                && other.hasTutSection()
                && mine.getTutSection()
                .equals(other.getTutSection());

            int points = SAME_COURSE_POINTS;

            if (sameLec) {
                points += SAME_LEC_BONUS;
            }

            if (sameTut) {
                points += SAME_TUT_BONUS;
            }

            totalScore += points;

            matchedCourses.add(
                new CourseMatchScore.MatchedCourse(
                    other.getCourseCode(),
                    other.getLecSection(),
                    other.getTutSection(),
                    sameLec,
                    sameTut,
                    points));
        }

        matchedCourses.sort(
            Comparator.comparing(
                CourseMatchScore.MatchedCourse::getCourseCode));

        return new CourseMatchScore(
            candidate,
            totalScore,
            matchedCourses);
    }
}
