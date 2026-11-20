package data_access.in_memory;

import entity.CourseEnrollment;
import entity.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import use_case.edit_schedule.EditScheduleDataAccessInterface;
import use_case.match_course_buddies.MatchCourseBuddiesDataAccessInterface;

/**
 * In-memory course schedule DAO implementing schedule management and course-match DAIs.
 */
public final class InMemoryScheduleDataAccess
        implements EditScheduleDataAccessInterface, MatchCourseBuddiesDataAccessInterface {

    private final InMemoryCampusBuddyStore store;

    public InMemoryScheduleDataAccess(InMemoryCampusBuddyStore store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    @Override
    public List<CourseEnrollment> findEnrollmentsByUser(String userId) {
        synchronized (store) {
            List<CourseEnrollment> result = new ArrayList<>();
            for (CourseEnrollment enrollment : store.enrollmentsByKey().values()) {
                if (enrollment.getUserId().equals(userId)) {
                    result.add(enrollment);
                }
            }
            result.sort(Comparator.comparing(CourseEnrollment::getCourseCode));
            return result;
        }
    }

    @Override
    public void saveEnrollment(CourseEnrollment enrollment) {
        synchronized (store) {
            store.putEnrollment(enrollment);
        }
    }

    @Override
    public void deleteEnrollment(String userId, String courseCode) {
        synchronized (store) {
            store.enrollmentsByKey()
                    .remove(InMemoryCampusBuddyStore.enrollmentKey(userId, courseCode));
        }
    }

    @Override
    public List<CourseEnrollment> findEnrollmentsByCourseCodes(Set<String> courseCodes) {
        synchronized (store) {
            List<CourseEnrollment> result = new ArrayList<>();
            if (courseCodes == null || courseCodes.isEmpty()) {
                return result;
            }
            for (CourseEnrollment enrollment : store.enrollmentsByKey().values()) {
                if (courseCodes.contains(enrollment.getCourseCode())) {
                    result.add(enrollment);
                }
            }
            return result;
        }
    }

    @Override
    public Map<String, User> findUsersByIds(Set<String> userIds) {
        synchronized (store) {
            Map<String, User> result = new LinkedHashMap<>();
            if (userIds == null) {
                return result;
            }
            for (String userId : userIds) {
                User user = store.usersById().get(userId);
                if (user != null) {
                    result.put(userId, user);
                }
            }
            return result;
        }
    }
}
