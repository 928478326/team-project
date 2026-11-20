package entity;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Domain entity representing a student's enrollment in a single UofT course section.
 * Enforces canonical formatting for course code, lecture section, and optional tutorial section.
 */
public final class CourseEnrollment {

    /** Pattern matching UofT course codes such as CSC207H1F or CSC207H1. */
    public static final Pattern COURSE_CODE_PATTERN =
            Pattern.compile("^[A-Z]{3}[0-9]{3}[HY][135][FSY]?$");
    /** Pattern matching lecture section codes such as LEC0101 or LEC5101. */
    public static final Pattern LEC_SECTION_PATTERN = Pattern.compile("^LEC[0-9]{4}$");
    /** Pattern matching tutorial section codes such as TUT0201 or PRA0101. */
    public static final Pattern TUT_SECTION_PATTERN = Pattern.compile("^(TUT|PRA)[0-9]{4}$");

    private final String userId;
    private final String courseCode;
    private final String lecSection;
    private final String tutSection;

    /**
     * Constructs an immutable {@code CourseEnrollment} and validates UofT section codes.
     *
     * @param userId enrolled student user ID (non-blank)
     * @param courseCode UofT course code (e.g., {@code CSC207H1F})
     * @param lecSection lecture section code (e.g., {@code LEC0101})
     * @param tutSection optional tutorial/practical section (e.g., {@code TUT0201}, or blank/null)
     */
    public CourseEnrollment(
            String userId, String courseCode, String lecSection, String tutSection) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank.");
        }
        this.userId = userId.trim();
        this.courseCode = normalizeCourseCode(courseCode);
        this.lecSection = normalizeLecSection(lecSection);
        this.tutSection = normalizeTutSection(tutSection);
    }

    /**
     * Validates and normalizes a UofT course code to uppercase.
     *
     * @param rawCourseCode raw course code input
     * @return canonical uppercase course code
     */
    public static String normalizeCourseCode(String rawCourseCode) {
        if (rawCourseCode == null || rawCourseCode.isBlank()) {
            throw new IllegalArgumentException("Course code must not be blank.");
        }
        String normalized = rawCourseCode.trim().toUpperCase(Locale.ROOT);
        if (!COURSE_CODE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Invalid UofT course code (expected e.g. CSC207H1F): " + rawCourseCode);
        }
        return normalized;
    }

    /**
     * Validates and normalizes a lecture section code to uppercase.
     *
     * @param rawLecSection raw lecture section input
     * @return canonical uppercase lecture section
     */
    public static String normalizeLecSection(String rawLecSection) {
        if (rawLecSection == null || rawLecSection.isBlank()) {
            throw new IllegalArgumentException("Lecture section must not be blank (e.g. LEC0101).");
        }
        String normalized = rawLecSection.trim().toUpperCase(Locale.ROOT);
        if (!LEC_SECTION_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Invalid lecture section (expected e.g. LEC0101): " + rawLecSection);
        }
        return normalized;
    }

    /**
     * Validates and normalizes an optional tutorial/practical section code.
     *
     * @param rawTutSection raw tutorial section input (may be null or blank)
     * @return canonical uppercase tutorial section, or {@code ""} if absent
     */
    public static String normalizeTutSection(String rawTutSection) {
        if (rawTutSection == null || rawTutSection.isBlank()) {
            return "";
        }
        String normalized = rawTutSection.trim().toUpperCase(Locale.ROOT);
        if (!TUT_SECTION_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Invalid tutorial section (expected e.g. TUT0201 or blank): " + rawTutSection);
        }
        return normalized;
    }

    public String getUserId() {
        return userId;
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

    /**
     * Checks whether this enrollment includes a tutorial/practical section.
     *
     * @return {@code true} if {@code tutSection} is non-empty
     */
    public boolean hasTutSection() {
        return !tutSection.isEmpty();
    }
}
