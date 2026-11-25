
package use_case.match_course_buddies;

import data_access.in_memory.InMemoryCampusBuddyStore;
import data_access.in_memory.InMemoryScheduleDataAccess;
import data_access.in_memory.Sha256PasswordHasher;
import entity.DefaultCourseMatchStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchCourseBuddiesInteractorTest {

  private MatchCourseBuddiesInteractor interactor;
  private TestPresenter presenter;

  @BeforeEach
  void setUp() {
    InMemoryCampusBuddyStore store =
        InMemoryCampusBuddyStore.createSeeded(
            new Sha256PasswordHasher());

    InMemoryScheduleDataAccess dataAccess =
        new InMemoryScheduleDataAccess(store);

    presenter = new TestPresenter();

    interactor = new MatchCourseBuddiesInteractor(
        dataAccess,
        new DefaultCourseMatchStrategy(),
        presenter);
  }

  @Test
  void testAllMatches() {
    interactor.execute(new MatchCourseBuddiesInputData(
        "u-henry", "ALL"));

    assertNotNull(presenter.output);
    assertNull(presenter.error);

    List<MatchCourseBuddiesOutputData.MatchItem> matches =
        presenter.output.getMatches();

    assertEquals(3, matches.size());

    assertEquals("u-gracie", matches.get(0).getUserId());
    assertEquals(60, matches.get(0).getMatchScore());

    assertEquals("u-james", matches.get(1).getUserId());
    assertEquals(30, matches.get(1).getMatchScore());

    assertEquals("u-alex", matches.get(2).getUserId());
    assertEquals(10, matches.get(2).getMatchScore());
  }

  @Test
  void testSameLectureFilter() {
    interactor.execute(new MatchCourseBuddiesInputData(
        "u-henry", "SAME_LEC"));

    assertNotNull(presenter.output);

    List<MatchCourseBuddiesOutputData.MatchItem> matches =
        presenter.output.getMatches();

    assertEquals(2, matches.size());
    assertEquals("u-gracie", matches.get(0).getUserId());
    assertEquals("u-james", matches.get(1).getUserId());
    assertNull(presenter.error);
  }

  @Test
  void testSameTutorialFilter() {
    interactor.execute(new MatchCourseBuddiesInputData(
        "u-henry", "SAME_TUT"));

    assertNotNull(presenter.output);

    List<MatchCourseBuddiesOutputData.MatchItem> matches =
        presenter.output.getMatches();

    assertEquals(1, matches.size());
    assertEquals("u-gracie", matches.get(0).getUserId());
    assertEquals(60, matches.get(0).getMatchScore());
    assertNull(presenter.error);
  }

  @Test
  void testExcludeCurrentUser() {
    interactor.execute(new MatchCourseBuddiesInputData(
        "u-henry", "ALL"));

    assertNotNull(presenter.output);

    for (MatchCourseBuddiesOutputData.MatchItem item
        : presenter.output.getMatches()) {
      assertNotEquals("u-henry", item.getUserId());
    }
  }

  @Test
  void testEmptySchedule() {
    interactor.execute(new MatchCourseBuddiesInputData(
        "u-herry", "ALL"));

    assertNotNull(presenter.output);
    assertTrue(presenter.output.getMatches().isEmpty());
    assertNull(presenter.error);
  }

  @Test
  void testInvalidFilter() {
    interactor.execute(new MatchCourseBuddiesInputData(
        "u-henry", "INVALID"));

    assertNotNull(presenter.error);
    assertNull(presenter.output);
  }

  @Test
  void testBlankUserId() {
    interactor.execute(new MatchCourseBuddiesInputData(
        "", "ALL"));

    assertNotNull(presenter.error);
    assertNull(presenter.output);
  }

  @Test
  void testOverlapSummary() {
    interactor.execute(new MatchCourseBuddiesInputData(
        "u-henry", "ALL"));

    assertNotNull(presenter.output);

    List<String> summaries = presenter.output
        .getMatches()
        .get(0)
        .getOverlapSummaries();

    assertEquals(1, summaries.size());
    assertTrue(summaries.get(0).contains("CSC207H1F"));
    assertTrue(summaries.get(0).contains("LEC0101"));
    assertTrue(summaries.get(0).contains("TUT0201"));
  }

  @Test
  void testEqualScoreSorting() {
    interactor.execute(new MatchCourseBuddiesInputData(
        "u-alex", "ALL"));

    assertNotNull(presenter.output);

    List<MatchCourseBuddiesOutputData.MatchItem> matches =
        presenter.output.getMatches();

    assertEquals(3, matches.size());

    assertEquals("u-gracie", matches.get(0).getUserId());
    assertEquals("u-henry", matches.get(1).getUserId());
    assertEquals("u-james", matches.get(2).getUserId());

    for (MatchCourseBuddiesOutputData.MatchItem item : matches) {
      assertEquals(10, item.getMatchScore());
    }
  }

  @Test
  void testNoMatchesAfterFiltering() {
    interactor.execute(new MatchCourseBuddiesInputData(
        "u-alex", "SAME_LEC"));

    assertNotNull(presenter.output);
    assertTrue(presenter.output.getMatches().isEmpty());
    assertEquals("No matching buddies found.",
        presenter.output.getInfoMessage());
    assertNull(presenter.error);
  }


  private static class TestPresenter
      implements MatchCourseBuddiesOutputBoundary {

    private MatchCourseBuddiesOutputData output;
    private String error;

    @Override
    public void prepareSuccessView(
        MatchCourseBuddiesOutputData outputData) {
      this.output = outputData;
      this.error = null;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      this.error = errorMessage;
      this.output = null;
    }
  }
}
