package interface_adapter.course_match;

import java.util.Objects;
import use_case.match_course_buddies.MatchCourseBuddiesInputBoundary;
import use_case.match_course_buddies.MatchCourseBuddiesInputData;

/**
 * Controller for finding study buddies based on course, LEC, and TUT overlap.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class MatchCourseBuddiesController {

    private final MatchCourseBuddiesInputBoundary interactor;

    public MatchCourseBuddiesController(MatchCourseBuddiesInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    /**
     * Triggers study-buddy matching with the specified filter mode.
     *
     * @param userId active user ID
     * @param filterMode {@code "ALL"}, {@code "SAME_LEC"}, or {@code "SAME_TUT"}
     */
    public void execute(String userId, String filterMode) {
        interactor.execute(new MatchCourseBuddiesInputData(userId, filterMode));
    }
}
