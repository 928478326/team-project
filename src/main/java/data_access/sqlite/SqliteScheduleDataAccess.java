package data_access.sqlite;

import entity.CourseEnrollment;
import entity.User;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import use_case.edit_schedule.EditScheduleDataAccessInterface;
import use_case.match_course_buddies.MatchCourseBuddiesDataAccessInterface;

/**
 * SQLite implementation of course-schedule and classmate-matching Data Access Interfaces.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class SqliteScheduleDataAccess
        implements EditScheduleDataAccessInterface, MatchCourseBuddiesDataAccessInterface {

    private final SqliteDatabase database;

    public SqliteScheduleDataAccess(SqliteDatabase database) {
        this.database = Objects.requireNonNull(database, "database must not be null");
    }

    @Override
    public List<CourseEnrollment> findEnrollmentsByUser(String userId) {
        // TODO [Track A]: SELECT * FROM course_enrollments WHERE user_id = ? ORDER BY course_code
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteScheduleDataAccess.findEnrollmentsByUser");
    }

    @Override
    public void saveEnrollment(CourseEnrollment enrollment) {
        // TODO [Track A]: INSERT INTO course_enrollments (...) VALUES (...)
        // ON CONFLICT(user_id, course_code) DO UPDATE SET lec_section=excluded.lec_section, tut_section=excluded.tut_section
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteScheduleDataAccess.saveEnrollment");
    }

    @Override
    public void deleteEnrollment(String userId, String courseCode) {
        // TODO [Track A]: DELETE FROM course_enrollments WHERE user_id = ? AND course_code = ?
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteScheduleDataAccess.deleteEnrollment");
    }

    @Override
    public List<CourseEnrollment> findEnrollmentsByCourseCodes(Set<String> courseCodes) {
        // TODO [Track A]: SELECT * FROM course_enrollments WHERE course_code IN (...)
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteScheduleDataAccess.findEnrollmentsByCourseCodes");
    }

    @Override
    public Map<String, User> findUsersByIds(Set<String> userIds) {
        // TODO [Track A]: SELECT * FROM users WHERE user_id IN (...)
        throw new UnsupportedOperationException("TODO [Track A]: Implement SqliteScheduleDataAccess.findUsersByIds");
    }
}
