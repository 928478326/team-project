package use_case.match_course_buddies;

/**
 * Output boundary for course study-buddy matching.
 */
public interface MatchCourseBuddiesOutputBoundary {

    /**
     * Presents ranked study buddy matches.
     *
     * @param outputData ranked peer matches and informational status
     */
    void prepareSuccessView(MatchCourseBuddiesOutputData outputData);

    /**
     * Presents an error if matching cannot be executed.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
