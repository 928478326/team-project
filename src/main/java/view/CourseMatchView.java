
package view;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.course_match.CourseMatchState;
import interface_adapter.course_match.CourseMatchViewModel;
import interface_adapter.course_match.EditScheduleController;
import interface_adapter.course_match.MatchCourseBuddiesController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Swing view for managing course schedules and finding study buddies.
 */
public final class CourseMatchView extends JPanel
    implements PropertyChangeListener {

  private final CourseMatchViewModel viewModel;
  private final SessionViewModel sessionViewModel;

  private EditScheduleController editScheduleController;
  private MatchCourseBuddiesController matchCourseBuddiesController;

  private final JLabel statusLabel = new JLabel(" ");

  private final JTextField courseCodeField = new JTextField(10);
  private final JTextField lecField = new JTextField(8);
  private final JTextField tutField = new JTextField(8);

  private final JButton addButton = new JButton("Add / Update");
  private final JButton removeButton = new JButton("Remove Selected");
  private final JButton matchButton = new JButton("Find Matches");

  private final JComboBox<String> filterBox =
      new JComboBox<>(new String[]{
          "ALL", "SAME_LEC", "SAME_TUT"
      });

  private final DefaultTableModel scheduleModel =
      createReadOnlyModel(
          "Course Code", "Title", "LEC", "TUT");

  private final DefaultTableModel matchModel =
      createReadOnlyModel(
          "Buddy", "Score", "Shared Courses", "Contact");

  private final JTable scheduleTable = new JTable(scheduleModel);
  private final JTable matchTable = new JTable(matchModel);

  private String activeUserId = "";

  public CourseMatchView(
      CourseMatchViewModel viewModel,
      SessionViewModel sessionViewModel) {

    this.viewModel = Objects.requireNonNull(viewModel);
    this.sessionViewModel =
        Objects.requireNonNull(sessionViewModel);

    setLayout(new BorderLayout(12, 12));
    setBorder(BorderFactory.createEmptyBorder(
        16, 16, 16, 16));

    JPanel content = new JPanel(
        new GridLayout(2, 1, 0, 12));

    content.add(createSchedulePanel());
    content.add(createMatchPanel());

    add(content, BorderLayout.CENTER);
    add(statusLabel, BorderLayout.SOUTH);

    registerListeners();

    this.viewModel.addPropertyChangeListener(this);
    this.sessionViewModel.addPropertyChangeListener(this);
  }

  private JPanel createSchedulePanel() {
    JPanel panel = new JPanel(new BorderLayout(8, 8));

    panel.setBorder(BorderFactory.createTitledBorder(
        "My Course Schedule"));

    scheduleTable.setSelectionMode(
        ListSelectionModel.SINGLE_SELECTION);

    scheduleTable.setPreferredScrollableViewportSize(
        new Dimension(800, 120));

    panel.add(new JScrollPane(scheduleTable),
        BorderLayout.CENTER);

    JPanel controls = new JPanel(
        new FlowLayout(FlowLayout.LEFT));

    controls.add(new JLabel("Course Code:"));
    controls.add(courseCodeField);

    controls.add(new JLabel("LEC:"));
    controls.add(lecField);

    controls.add(new JLabel("TUT:"));
    controls.add(tutField);

    controls.add(addButton);
    controls.add(removeButton);

    panel.add(controls, BorderLayout.SOUTH);

    return panel;
  }

  private JPanel createMatchPanel() {
    JPanel panel = new JPanel(new BorderLayout(8, 8));

    panel.setBorder(BorderFactory.createTitledBorder(
        "Find Course Buddies"));

    JPanel controls = new JPanel(
        new FlowLayout(FlowLayout.LEFT));

    controls.add(new JLabel("Filter:"));
    controls.add(filterBox);
    controls.add(matchButton);

    panel.add(controls, BorderLayout.NORTH);

    matchTable.setPreferredScrollableViewportSize(
        new Dimension(800, 160));

    matchTable.getColumnModel()
        .getColumn(2).setPreferredWidth(350);

    panel.add(new JScrollPane(matchTable),
        BorderLayout.CENTER);

    return panel;
  }

  private void registerListeners() {
    addButton.addActionListener(e -> addCourse());

    removeButton.addActionListener(e -> removeCourse());

    matchButton.addActionListener(e -> findMatches());

    filterBox.addActionListener(e -> findMatches());
  }

  private void addCourse() {
    syncSession();

    if (activeUserId.isBlank()
        || editScheduleController == null) {
      return;
    }

    editScheduleController.addCourse(
        activeUserId,
        courseCodeField.getText(),
        lecField.getText(),
        tutField.getText());

    if (viewModel.getState().getErrorMessage().isBlank()) {
      invalidateMatches();
    }
  }

  private void removeCourse() {
    syncSession();

    if (activeUserId.isBlank()
        || editScheduleController == null) {
      return;
    }

    int selectedRow = scheduleTable.getSelectedRow();

    if (selectedRow == -1) {
      statusLabel.setText("Select a course to remove.");
      return;
    }

    int modelRow =
        scheduleTable.convertRowIndexToModel(selectedRow);

    String courseCode =
        (String) scheduleModel.getValueAt(modelRow, 0);

    editScheduleController.removeCourse(
        activeUserId, courseCode);

    if (viewModel.getState().getErrorMessage().isBlank()) {
      invalidateMatches();
    }
  }

  private void findMatches() {
    syncSession();

    if (activeUserId.isBlank()
        || matchCourseBuddiesController == null) {
      return;
    }

    String filterMode =
        (String) filterBox.getSelectedItem();

    matchCourseBuddiesController.execute(
        activeUserId, filterMode);
  }

  public String getViewName() {
    return viewModel.getViewName();
  }

  public void setEditScheduleController(
      EditScheduleController controller) {
    this.editScheduleController = controller;
  }

  public void setMatchCourseBuddiesController(
      MatchCourseBuddiesController controller) {
    this.matchCourseBuddiesController = controller;
  }

  /**
   * Reloads the current user's schedule when
   * the Course Match tab becomes active.
   */
  public void refreshData() {
    syncSession();

    if (!activeUserId.isBlank()
        && editScheduleController != null) {

      invalidateMatches();

      editScheduleController.loadSchedule(activeUserId);
    }
  }

  private void invalidateMatches() {
    viewModel.getState().setMatches(List.of());
    matchModel.setRowCount(0);
  }

  private void syncSession() {
    SessionState session = sessionViewModel.getState();

    String currentUserId = session.isLoggedIn()
        ? session.getUserId()
        : "";

    if (!currentUserId.equals(activeUserId)) {
      activeUserId = currentUserId;

      viewModel.setState(new CourseMatchState());

      courseCodeField.setText("");
      lecField.setText("");
      tutField.setText("");

      renderState();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    Runnable updateTask = () -> {
      if (event.getSource() == sessionViewModel) {
        syncSession();
      } else if (event.getSource() == viewModel) {
        renderState();
      }
    };

    if (SwingUtilities.isEventDispatchThread()) {
      updateTask.run();
    } else {
      SwingUtilities.invokeLater(updateTask);
    }
  }

  private void renderState() {
    CourseMatchState state = viewModel.getState();

    scheduleModel.setRowCount(0);
    matchModel.setRowCount(0);

    if (activeUserId.isBlank()) {
      statusLabel.setText("Signed out.");
      return;
    }

    for (CourseMatchState.EnrollmentRow row
        : state.getEnrollments()) {

      scheduleModel.addRow(new Object[]{
          row.getCourseCode(),
          row.getCourseTitle(),
          row.getLecSection(),
          row.getTutSection()
      });
    }

    for (CourseMatchState.MatchRow row
        : state.getMatches()) {

      matchModel.addRow(new Object[]{
          row.getDisplayName(),
          row.getMatchScore(),
          String.join(", ", row.getOverlapSummaries()),
          row.getContactInfo()
      });
    }

    if (!state.getErrorMessage().isBlank()) {
      statusLabel.setText(
          "Error: " + state.getErrorMessage());
    } else {
      statusLabel.setText(state.getStatusMessage());
    }
  }

  private static DefaultTableModel createReadOnlyModel(
      String... columns) {

    return new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
  }
}
