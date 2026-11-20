package use_case.match_course_buddies;

import entity.CourseMatchStrategy;
import java.util.Objects;

/**
 * Interactor implementing tiered UofT study-buddy matching (Same Course / Same LEC / Same TUT).
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class MatchCourseBuddiesInteractor implements MatchCourseBuddiesInputBoundary {

    private final MatchCourseBuddiesDataAccessInterface dataAccess;
    private final CourseMatchStrategy scoringStrategy;
    private final MatchCourseBuddiesOutputBoundary outputBoundary;

    public MatchCourseBuddiesInteractor(
            MatchCourseBuddiesDataAccessInterface dataAccess,
            CourseMatchStrategy scoringStrategy,
            MatchCourseBuddiesOutputBoundary outputBoundary) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.scoringStrategy =
                Objects.requireNonNull(scoringStrategy, "scoringStrategy must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
    }

    @Override
    public void execute(MatchCourseBuddiesInputData inputData) {
        // TODO [Track C]:
        // 1. Validate userId and filterMode (ALL / SAME_LEC / SAME_TUT).
        // 2. Load myEnrollments; if empty, present empty matches with guidance infoMessage.
        // 3. Query findEnrollmentsByCourseCodes(...), group other users' enrollments by userId,
        //    fetch candidate Users via findUsersByIds(...).
        // 4. Score each candidate with scoringStrategy.evaluate(...), apply filterMode
        //    (hasSameLec / hasSameTut), sort by totalScore DESC then userId ASC.
        // 5. Map to MatchCourseBuddiesOutputData.MatchItem and call prepareSuccessView(...).
        outputBoundary.prepareFailView(
                "TODO [Track C]: Implement MatchCourseBuddiesInteractor.execute");
    }
}
