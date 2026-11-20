package view;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.course_match.CourseMatchState;
import interface_adapter.course_match.CourseMatchViewModel;
import interface_adapter.course_match.EditScheduleController;
import interface_adapter.course_match.MatchCourseBuddiesController;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Course Schedule &amp; Study Buddy Match Swing tab: manage UofT course/LEC/TUT schedule
 * and discover classmates filtered by All / Same LEC / Same TUT.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class CourseMatchView extends JPanel implements PropertyChangeListener {

    private final CourseMatchViewModel viewModel;
    private final SessionViewModel sessionViewModel;
    private final JLabel statusLabel =
            new JLabel("TODO [Track C]: Build CourseMatchView (Schedule Editor + Classmate Matches)");

    private EditScheduleController editScheduleController;
    private MatchCourseBuddiesController matchCourseBuddiesController;

    public CourseMatchView(
            CourseMatchViewModel viewModel, SessionViewModel sessionViewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.sessionViewModel =
                Objects.requireNonNull(sessionViewModel, "sessionViewModel must not be null");
        this.viewModel.addPropertyChangeListener(this);
        this.sessionViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(statusLabel, BorderLayout.NORTH);
    }

    public String getViewName() {
        return viewModel.getViewName();
    }

    public void setEditScheduleController(EditScheduleController editScheduleController) {
        this.editScheduleController = editScheduleController;
    }

    public void setMatchCourseBuddiesController(
            MatchCourseBuddiesController matchCourseBuddiesController) {
        this.matchCourseBuddiesController = matchCourseBuddiesController;
    }

    /**
     * Triggered by {@link MainShellView} when this tab becomes visible.
     */
    public void refreshData() {
        SessionState session = sessionViewModel.getState();
        if (session.isLoggedIn() && editScheduleController != null) {
            editScheduleController.loadSchedule(session.getUserId());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Runnable updateTask =
                () -> {
                    if (evt.getSource() == sessionViewModel) {
                        SessionState session = (SessionState) evt.getNewValue();
                        if (session != null && !session.isLoggedIn()) {
                            // TODO [Track C]: Clear schedule & match tables on logout.
                            statusLabel.setText("Signed out.");
                        }
                        return;
                    }
                    if (evt.getSource() == viewModel) {
                        CourseMatchState state = (CourseMatchState) evt.getNewValue();
                        // TODO [Track C]: Render enrollments list, add/remove controls, filter toggles, and matches.
                        String text = !state.getErrorMessage().isBlank()
                                ? "Error: " + state.getErrorMessage()
                                : state.getStatusMessage();
                        if (!text.isBlank()) {
                            statusLabel.setText(text);
                        }
                    }
                };
        if (SwingUtilities.isEventDispatchThread()) {
            updateTask.run();
        } else {
            SwingUtilities.invokeLater(updateTask);
        }
    }
}
