
package use_case.match_course_buddies;

import entity.CourseEnrollment;
import entity.CourseMatchScore;
import entity.CourseMatchStrategy;
import entity.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Interactor for finding study buddies based on
 * shared courses, lecture sections and tutorial sections.
 */
public final class MatchCourseBuddiesInteractor
    implements MatchCourseBuddiesInputBoundary {

    private final MatchCourseBuddiesDataAccessInterface dataAccess;
    private final CourseMatchStrategy scoringStrategy;
    private final MatchCourseBuddiesOutputBoundary outputBoundary;

    public MatchCourseBuddiesInteractor(
        MatchCourseBuddiesDataAccessInterface dataAccess,
        CourseMatchStrategy scoringStrategy,
        MatchCourseBuddiesOutputBoundary outputBoundary) {

        this.dataAccess = Objects.requireNonNull(dataAccess);
        this.scoringStrategy = Objects.requireNonNull(scoringStrategy);
        this.outputBoundary = Objects.requireNonNull(outputBoundary);
    }

    @Override
    public void execute(MatchCourseBuddiesInputData inputData) {

        String userId = inputData.getUserId();
        String filterMode = inputData.getFilterMode();

        // Validate input.
        if (userId.isBlank()) {
            outputBoundary.prepareFailView(
                "User ID must not be blank.");
            return;
        }

        if (!isValidFilter(filterMode)) {
            outputBoundary.prepareFailView(
                "Invalid match filter.");
            return;
        }

        try {
            findMatches(userId, filterMode);
        } catch (UnsupportedOperationException | IllegalStateException exception) {
            outputBoundary.prepareFailView(
                "Course matching storage is unavailable. Please try again.");
        }
    }

    private void findMatches(String userId, String filterMode) {
        // Load the current user's schedule.
        List<CourseEnrollment> myEnrollments =
            dataAccess.findEnrollmentsByUser(userId);

        if (myEnrollments.isEmpty()) {
            outputBoundary.prepareSuccessView(
                new MatchCourseBuddiesOutputData(
                    filterMode,
                    List.of(),
                    "Add courses to your schedule first."));
            return;
        }

        // Collect course codes.
        Set<String> courseCodes = new HashSet<>();

        for (CourseEnrollment enrollment : myEnrollments) {
            courseCodes.add(enrollment.getCourseCode());
        }

        // Find enrollments sharing those course codes.
        List<CourseEnrollment> allEnrollments =
            dataAccess.findEnrollmentsByCourseCodes(courseCodes);

        // Group candidate enrollments and exclude the current user.
        Map<String, List<CourseEnrollment>> candidateEnrollments =
            new HashMap<>();

        for (CourseEnrollment enrollment : allEnrollments) {

            if (userId.equals(enrollment.getUserId())) {
                continue;
            }

            candidateEnrollments
                .computeIfAbsent(
                    enrollment.getUserId(),
                    key -> new ArrayList<>())
                .add(enrollment);
        }

        // Retrieve candidate users.
        Map<String, User> candidates =
            dataAccess.findUsersByIds(
                candidateEnrollments.keySet());

        // Evaluate and filter matches.
        List<CourseMatchScore> scores = new ArrayList<>();

        for (User candidate : candidates.values()) {

            CourseMatchScore score = scoringStrategy.evaluate(
                myEnrollments,
                candidate,
                candidateEnrollments.get(candidate.getUserId()));

            if (score.getTotalScore() > 0
                && matchesFilter(score, filterMode)) {
                scores.add(score);
            }
        }

        // Sort by score descending, then user ID ascending.
        scores.sort(
            Comparator.comparingInt(
                    CourseMatchScore::getTotalScore)
                .reversed()
                .thenComparing(
                    score -> score.getCandidate().getUserId()));

        // Convert domain results into output data.
        List<MatchCourseBuddiesOutputData.MatchItem> matches =
            new ArrayList<>();

        for (CourseMatchScore score : scores) {
            matches.add(toMatchItem(score));
        }

        String message = matches.isEmpty()
            ? "No matching buddies found."
            : matches.size() + " matching buddies found.";

        outputBoundary.prepareSuccessView(
            new MatchCourseBuddiesOutputData(
                filterMode,
                matches,
                message));
    }

    private boolean isValidFilter(String filterMode) {
        return MatchCourseBuddiesInputData.FILTER_ALL.equals(filterMode)
            || MatchCourseBuddiesInputData.FILTER_SAME_LEC.equals(filterMode)
            || MatchCourseBuddiesInputData.FILTER_SAME_TUT.equals(filterMode);
    }

    private boolean matchesFilter(
        CourseMatchScore score, String filterMode) {

        switch (filterMode) {
            case MatchCourseBuddiesInputData.FILTER_SAME_LEC:
                return score.hasSameLec();

            case MatchCourseBuddiesInputData.FILTER_SAME_TUT:
                return score.hasSameTut();

            default:
                return true;
        }
    }

    private MatchCourseBuddiesOutputData.MatchItem toMatchItem(
        CourseMatchScore score) {

        User candidate = score.getCandidate();

        List<String> summaries = new ArrayList<>();

        for (CourseMatchScore.MatchedCourse course
            : score.getMatchedCourses()) {

            StringBuilder summary =
                new StringBuilder(course.getCourseCode());

            if (course.isSameLec()) {
                summary.append(" / ")
                    .append(course.getLecSection());
            }

            if (course.isSameTut()) {
                summary.append(" / ")
                    .append(course.getTutSection());
            }

            summary.append(" (+")
                .append(course.getPoints())
                .append(" pts)");

            summaries.add(summary.toString());
        }

        return new MatchCourseBuddiesOutputData.MatchItem(
            candidate.getUserId(),
            candidate.getDisplayName(),
            candidate.getWeekendStyle().getDisplayName(),
            candidate.getPublicContactInfo(),
            score.getTotalScore(),
            summaries);
    }
}
