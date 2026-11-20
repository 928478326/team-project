package use_case.edit_schedule;

import java.util.Objects;

/**
 * Immutable input data for managing a student's course schedule.
 */
public final class EditScheduleInputData {

    /** Supported schedule management actions. */
    public enum Action {
        LOAD,
        ADD,
        REMOVE
    }

    private final String userId;
    private final Action action;
    private final String courseCode;
    private final String lecSection;
    private final String tutSection;

    public EditScheduleInputData(
            String userId, Action action, String courseCode, String lecSection, String tutSection) {
        this.userId = userId == null ? "" : userId.trim();
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.courseCode = courseCode == null ? "" : courseCode.trim();
        this.lecSection = lecSection == null ? "" : lecSection.trim();
        this.tutSection = tutSection == null ? "" : tutSection.trim();
    }

    public String getUserId() {
        return userId;
    }

    public Action getAction() {
        return action;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getLecSection() {
        return lecSection;
    }

    public String getTutSection() {
        return tutSection;
    }
}
