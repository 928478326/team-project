package use_case.edit_schedule;

import data_access.gateway.LocalUoftCourseCatalogGateway;
import data_access.in_memory.InMemoryCampusBuddyStore;
import data_access.in_memory.InMemoryScheduleDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditScheduleInteractorTest {

  private static final String USER_ID = "u-test";

  private InMemoryScheduleDataAccess dataAccess;
  private TestPresenter presenter;
  private EditScheduleInteractor interactor;

  @BeforeEach
  void setUp() {
    dataAccess = new InMemoryScheduleDataAccess(
        new InMemoryCampusBuddyStore());

    presenter = new TestPresenter();

    interactor = new EditScheduleInteractor(
        dataAccess,
        new LocalUoftCourseCatalogGateway(),
        presenter);
  }

  @Test
  void testLoadEmptySchedule() {
    interactor.execute(new EditScheduleInputData(
        USER_ID, EditScheduleInputData.Action.LOAD,
        "", "", ""));

    assertNotNull(presenter.output);
    assertTrue(presenter.output.getEnrollments().isEmpty());
    assertNull(presenter.error);
  }

  @Test
  void testAddCourse() {
    interactor.execute(new EditScheduleInputData(
        USER_ID, EditScheduleInputData.Action.ADD,
        "csc207h1f", "lec0101", "tut0201"));

    assertNotNull(presenter.output);
    assertEquals(1, presenter.output.getEnrollments().size());

    EditScheduleOutputData.EnrollmentItem item =
        presenter.output.getEnrollments().get(0);

    assertEquals("CSC207H1F", item.getCourseCode());
    assertEquals("LEC0101", item.getLecSection());
    assertEquals("TUT0201", item.getTutSection());
    assertEquals("Software Design", item.getCourseTitle());

    assertEquals(1,
        dataAccess.findEnrollmentsByUser(USER_ID).size());
    assertNull(presenter.error);
  }

  @Test
  void testRemoveCourse() {
    interactor.execute(new EditScheduleInputData(
        USER_ID, EditScheduleInputData.Action.ADD,
        "CSC207H1F", "LEC0101", "TUT0201"));

    interactor.execute(new EditScheduleInputData(
        USER_ID, EditScheduleInputData.Action.REMOVE,
        "CSC207H1F", "", ""));

    assertTrue(presenter.output.getEnrollments().isEmpty());
    assertTrue(dataAccess.findEnrollmentsByUser(USER_ID).isEmpty());
    assertNull(presenter.error);
  }

  @Test
  void testUpdateExistingCourse() {
    interactor.execute(new EditScheduleInputData(
        USER_ID, EditScheduleInputData.Action.ADD,
        "CSC207H1F", "LEC0101", "TUT0201"));

    interactor.execute(new EditScheduleInputData(
        USER_ID, EditScheduleInputData.Action.ADD,
        "CSC207H1F", "LEC0201", "TUT0301"));

    assertEquals(1, presenter.output.getEnrollments().size());
    assertEquals("LEC0201",
        presenter.output.getEnrollments().get(0).getLecSection());
    assertEquals("TUT0301",
        presenter.output.getEnrollments().get(0).getTutSection());
    assertNull(presenter.error);
  }

  @Test
  void testInvalidCourseCode() {
    interactor.execute(new EditScheduleInputData(
        USER_ID, EditScheduleInputData.Action.ADD,
        "INVALID", "LEC0101", "TUT0201"));

    assertNotNull(presenter.error);
    assertNull(presenter.output);
    assertTrue(dataAccess.findEnrollmentsByUser(USER_ID).isEmpty());
  }

  @Test
  void testBlankUserId() {
    interactor.execute(new EditScheduleInputData(
        "", EditScheduleInputData.Action.LOAD,
        "", "", ""));

    assertNotNull(presenter.error);
    assertNull(presenter.output);
  }

  private static class TestPresenter
      implements EditScheduleOutputBoundary {

    private EditScheduleOutputData output;
    private String error;

    @Override
    public void prepareSuccessView(EditScheduleOutputData outputData) {
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
