package view;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.calendar.CalendarState;
import interface_adapter.calendar.CalendarViewModel;
import interface_adapter.calendar.ViewCalendarController;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Personal Monthly Calendar Swing tab: displays joined events in Toronto local time,
 * Canadian/Ontario public holidays, and supports exporting to {@code .ics}.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 *
 * <p>Note: Run remote holiday API refreshes via {@link javax.swing.SwingWorker} inside this View
 * (never in Interactor or Controller) so the EDT remains responsive.
 */
public final class CalendarView extends JPanel implements PropertyChangeListener {

    private final CalendarViewModel viewModel;
    private final SessionViewModel sessionViewModel;
    private final JLabel statusLabel =
            new JLabel("TODO [Track B]: Build CalendarView (Month Nav + Holidays + .ics Export)");

    private ViewCalendarController viewCalendarController;

    public CalendarView(CalendarViewModel viewModel, SessionViewModel sessionViewModel) {
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

    public void setViewCalendarController(ViewCalendarController viewCalendarController) {
        this.viewCalendarController = viewCalendarController;
    }

    /**
     * Triggered by {@link MainShellView} when this tab becomes visible.
     */
    public void refreshData() {
        SessionState session = sessionViewModel.getState();
        if (session.isLoggedIn() && viewCalendarController != null) {
            CalendarState state = viewModel.getState();
            viewCalendarController.loadMonth(
                    session.getUserId(), state.getYear(), state.getMonth());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Runnable updateTask =
                () -> {
                    if (evt.getSource() == sessionViewModel) {
                        SessionState session = (SessionState) evt.getNewValue();
                        if (session != null && !session.isLoggedIn()) {
                            // TODO [Track B]: Clear displayed calendar rows on logout.
                            statusLabel.setText("Signed out.");
                        }
                        return;
                    }
                    if (evt.getSource() == viewModel) {
                        CalendarState state = (CalendarState) evt.getNewValue();
                        // TODO [Track B]: Render month header, holiday banner, event table, and export button.
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
