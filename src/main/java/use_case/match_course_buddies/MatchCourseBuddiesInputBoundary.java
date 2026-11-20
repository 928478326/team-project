package use_case.match_course_buddies;

/**
 * Input boundary for finding study buddies by course, lecture, and tutorial overlap.
 */
public interface MatchCourseBuddiesInputBoundary {

    /**
     * Ranks peers who share at least one course with the current user.
     *
     * @param inputData user ID and optional filter mode (ALL / SAME_LEC / SAME_TUT)
     */
    void execute(MatchCourseBuddiesInputData inputData);
}
