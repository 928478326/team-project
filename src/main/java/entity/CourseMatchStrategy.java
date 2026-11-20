package entity;

import java.util.List;

/**
 * Strategy interface for evaluating course-schedule similarity between two students.
 * Enables open-closed swapping of study-buddy matching policies (Strategy Pattern + OCP).
 */
public interface CourseMatchStrategy {

    /**
     * Evaluates overlap between the current student's enrollments and a candidate's enrollments.
     *
     * @param myEnrollments current user's enrollments
     * @param candidate candidate peer user
     * @param candidateEnrollments candidate's enrollments
     * @return immutable {@link CourseMatchScore} describing total score and per-course overlap
     */
    CourseMatchScore evaluate(
            List<CourseEnrollment> myEnrollments,
            User candidate,
            List<CourseEnrollment> candidateEnrollments);
}
