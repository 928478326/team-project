
package entity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCourseMatchStrategyTest {

  private final CourseMatchStrategy strategy =
      new DefaultCourseMatchStrategy();

  private final User candidate = new User(
      "u-candidate",
      "candidate",
      "test-hash",
      "Candidate",
      "",
      "",
      WeekendStyle.STUDY_FOCUS,
      Set.of());

  private final CourseEnrollment myCourse =
      new CourseEnrollment(
          "u-me",
          "CSC207H1F",
          "LEC0101",
          "TUT0201");

  private CourseMatchScore evaluate(
      String courseCode, String lec, String tut) {

    CourseEnrollment other = new CourseEnrollment(
        candidate.getUserId(), courseCode, lec, tut);

    return strategy.evaluate(
        List.of(myCourse),
        candidate,
        List.of(other));
  }

  @Test
  void testNoOverlap() {
    CourseMatchScore result =
        evaluate("CSC209H1S", "LEC0101", "TUT0201");

    assertEquals(0, result.getTotalScore());
    assertTrue(result.getMatchedCourses().isEmpty());
  }

  @Test
  void testSameCourseOnly() {
    CourseMatchScore result =
        evaluate("CSC207H1F", "LEC0201", "TUT0301");

    assertEquals(10, result.getTotalScore());
    assertFalse(result.hasSameLec());
    assertFalse(result.hasSameTut());
  }

  @Test
  void testSameLecture() {
    CourseMatchScore result =
        evaluate("CSC207H1F", "LEC0101", "TUT0301");

    assertEquals(30, result.getTotalScore());
    assertTrue(result.hasSameLec());
    assertFalse(result.hasSameTut());
  }

  @Test
  void testSameTutorial() {
    CourseMatchScore result =
        evaluate("CSC207H1F", "LEC0201", "TUT0201");

    assertEquals(40, result.getTotalScore());
    assertFalse(result.hasSameLec());
    assertTrue(result.hasSameTut());
  }

  @Test
  void testFullMatch() {
    CourseMatchScore result =
        evaluate("CSC207H1F", "LEC0101", "TUT0201");

    assertEquals(60, result.getTotalScore());
    assertTrue(result.hasSameLec());
    assertTrue(result.hasSameTut());
    assertEquals(1, result.getMatchedCourses().size());
  }

  @Test
  void testEmptyTutorialDoesNotMatch() {
    CourseEnrollment mine = new CourseEnrollment(
        "u-me", "CSC207H1F", "LEC0101", "");

    CourseEnrollment other = new CourseEnrollment(
        candidate.getUserId(),
        "CSC207H1F", "LEC0101", "");

    CourseMatchScore result = strategy.evaluate(
        List.of(mine),
        candidate,
        List.of(other));

    assertEquals(30, result.getTotalScore());
    assertFalse(result.hasSameTut());
  }

  @Test
  void testMultipleCourses() {
    CourseEnrollment mySecondCourse =
        new CourseEnrollment(
            "u-me", "MAT237Y1Y",
            "LEC0101", "TUT0101");

    CourseEnrollment otherFirstCourse =
        new CourseEnrollment(
            candidate.getUserId(),
            "CSC207H1F", "LEC0101", "TUT0201");

    CourseEnrollment otherSecondCourse =
        new CourseEnrollment(
            candidate.getUserId(),
            "MAT237Y1Y", "LEC0201", "TUT0201");

    CourseMatchScore result = strategy.evaluate(
        List.of(myCourse, mySecondCourse),
        candidate,
        List.of(otherSecondCourse, otherFirstCourse));

    assertEquals(70, result.getTotalScore());
    assertEquals(2, result.getMatchedCourses().size());

    assertEquals("CSC207H1F",
        result.getMatchedCourses().get(0).getCourseCode());

    assertEquals("MAT237Y1Y",
        result.getMatchedCourses().get(1).getCourseCode());
  }

  @Test
  void testEmptySchedule() {
    CourseMatchScore result = strategy.evaluate(
        List.of(), candidate, List.of());

    assertEquals(0, result.getTotalScore());
    assertTrue(result.getMatchedCourses().isEmpty());
  }
}
