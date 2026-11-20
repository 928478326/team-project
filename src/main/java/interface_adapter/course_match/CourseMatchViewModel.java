package interface_adapter.course_match;

import interface_adapter.ViewModel;

/**
 * ViewModel for the Course Schedule &amp; Study Buddy Match view.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class CourseMatchViewModel extends ViewModel<CourseMatchState> {

    public static final String VIEW_NAME = "course_match";

    public CourseMatchViewModel() {
        super(VIEW_NAME);
        setState(new CourseMatchState());
    }
}
