package use_case.edit_schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable output data for a student's course schedule.
 */
public final class EditScheduleOutputData {

    private final List<EnrollmentItem> enrollments;
    private final String statusMessage;

    public EditScheduleOutputData(List<EnrollmentItem> enrollments, String statusMessage) {
        this.enrollments = enrollments == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(enrollments));
        this.statusMessage = statusMessage == null ? "" : statusMessage;
    }

    public List<EnrollmentItem> getEnrollments() {
        return enrollments;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Immutable DTO describing one enrolled UofT course.
     */
    public static final class EnrollmentItem {

        private final String courseCode;
        private final String courseTitle;
        private final String lecSection;
        private final String tutSection;

        public EnrollmentItem(
                String courseCode, String courseTitle, String lecSection, String tutSection) {
            this.courseCode = courseCode;
            this.courseTitle = courseTitle == null ? "" : courseTitle;
            this.lecSection = lecSection;
            this.tutSection = tutSection == null ? "" : tutSection;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public String getLecSection() {
            return lecSection;
        }

        public String getTutSection() {
            return tutSection;
        }
    }
}
