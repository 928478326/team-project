package data_access.gateway;

import java.util.Map;
import java.util.Optional;
import use_case.edit_schedule.CourseCatalogGateway;

/**
 * Local built-in UofT St. George course catalog gateway guaranteeing 100% offline availability.
 * Additional implementations (e.g., an external UofT Timetable HTTP gateway) can be plugged in
 * without modifying {@link use_case.edit_schedule.EditScheduleInteractor} (OCP/DIP).
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class LocalUoftCourseCatalogGateway implements CourseCatalogGateway {

    private static final Map<String, String> CATALOG =
            Map.ofEntries(
                    Map.entry("CSC207H1F", "Software Design"),
                    Map.entry("CSC207H1", "Software Design"),
                    Map.entry("CSC209H1S", "Software Tools and Systems Programming"),
                    Map.entry("CSC236H1F", "Introduction to the Theory of Computation"),
                    Map.entry("CSC263H1S", "Data Structures and Analysis"),
                    Map.entry("MAT137Y1Y", "Calculus with Proofs"),
                    Map.entry("MAT223H1F", "Linear Algebra I"),
                    Map.entry("STA247H1F", "Probability with Computer Applications"),
                    Map.entry("ECO101H1F", "Principles of Microeconomics"));

    @Override
    public Optional<String> findCourseTitle(String normalizedCourseCode) {
        // TODO [Track C]: Expand UofT St. George course catalog or optionally decorate with a
        // remote timetable lookup fallback.
        if (normalizedCourseCode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(CATALOG.get(normalizedCourseCode));
    }
}
