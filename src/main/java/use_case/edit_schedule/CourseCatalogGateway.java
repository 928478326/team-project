package use_case.edit_schedule;

import java.util.Optional;

/**
 * Gateway abstraction for resolving UofT course titles from a local catalog or timetable API (OCP/DIP).
 */
public interface CourseCatalogGateway {

    /**
     * Looks up a human-readable course title for a canonical UofT course code (e.g., CSC207H1F).
     *
     * @param normalizedCourseCode canonical uppercase UofT course code
     * @return course title if known in the catalog
     */
    Optional<String> findCourseTitle(String normalizedCourseCode);
}
