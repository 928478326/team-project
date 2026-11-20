package use_case.match_course_buddies;

import entity.CourseEnrollment;
import entity.User;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Narrow Data Access Interface for course-schedule buddy matching (ISP).
 */
public interface MatchCourseBuddiesDataAccessInterface {

    /**
     * Loads all enrollments belonging to {@code userId}.
     *
     * @param userId student user ID
     * @return list of enrollments
     */
    List<CourseEnrollment> findEnrollmentsByUser(String userId);

    /**
     * Loads all enrollments across all users matching any of {@code courseCodes}.
     *
     * @param courseCodes canonical uppercase course codes
     * @return matching enrollments
     */
    List<CourseEnrollment> findEnrollmentsByCourseCodes(Set<String> courseCodes);

    /**
     * Batch-fetches users keyed by {@code userId}.
     *
     * @param userIds candidate user IDs
     * @return map of userId to {@link User}
     */
    Map<String, User> findUsersByIds(Set<String> userIds);
}
