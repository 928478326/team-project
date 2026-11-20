package view;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.buddy_rating.BuddyRatingState;
import interface_adapter.buddy_rating.BuddyRatingViewModel;
import interface_adapter.buddy_rating.RateBuddyController;
import interface_adapter.buddy_rating.ViewRateableBuddiesController;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Post-Meetup Buddy Rating Swing tab: lists peers who shared an event with the current user
 * and allows submitting/updating 1-5 star ratings, would-meet-again flags, and short comments.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class BuddyRatingView extends JPanel implements PropertyChangeListener {

    private final BuddyRatingViewModel viewModel;
    private final SessionViewModel sessionViewModel;
    private final JLabel statusLabel =
            new JLabel("TODO [Track C]: Build BuddyRatingView (Shared-Event Buddies + Rating Form)");

    private ViewRateableBuddiesController viewRateableBuddiesController;
    private RateBuddyController rateBuddyController;

    public BuddyRatingView(
            BuddyRatingViewModel viewModel, SessionViewModel sessionViewModel) {
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

    public void setViewRateableBuddiesController(
            ViewRateableBuddiesController viewRateableBuddiesController) {
        this.viewRateableBuddiesController = viewRateableBuddiesController;
    }

    public void setRateBuddyController(RateBuddyController rateBuddyController) {
        this.rateBuddyController = rateBuddyController;
    }

    /**
     * Triggered by {@link MainShellView} when this tab becomes visible.
     */
    public void refreshData() {
        SessionState session = sessionViewModel.getState();
        if (session.isLoggedIn() && viewRateableBuddiesController != null) {
            viewRateableBuddiesController.execute(session.getUserId());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Runnable updateTask =
                () -> {
                    if (evt.getSource() == sessionViewModel) {
                        SessionState session = (SessionState) evt.getNewValue();
                        if (session != null && !session.isLoggedIn()) {
                            // TODO [Track C]: Clear rateable buddy rows on logout.
                            statusLabel.setText("Signed out.");
                        }
                        return;
                    }
                    if (evt.getSource() == viewModel) {
                        BuddyRatingState state = (BuddyRatingState) evt.getNewValue();
                        // TODO [Track C]: Render rateable buddy rows and rating submit controls.
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
