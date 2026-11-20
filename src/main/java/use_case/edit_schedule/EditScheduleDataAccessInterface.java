package use_case.edit_schedule;

import entity.CourseEnrollment;
import java.util.List;

/**
 * Narrow Data Access Interface for reading and modifying a user's course schedule (ISP).
 */
public interface EditScheduleDataAccessInterface {

    /**
     * Returns all enrollments for {@code userId}, ordered by {@code courseCode ASC}.
     *
     * @param userId student user ID
     * @return list of enrollments
     */
    List<CourseEnrollment> findEnrollmentsByUser(String userId);

    /**
     * Inserts or updates an enrollment keyed by {@code (userId, courseCode)}.
     *
     * @param enrollment validated course enrollment entity
     */
    void saveEnrollment(CourseEnrollment enrollment);

    /**
     * Deletes a course enrollment for {@code userId}.
     *
     * @param userId student user ID
     * @param courseCode canonical uppercase course code
     */
    void deleteEnrollment(String userId, String courseCode);
}
