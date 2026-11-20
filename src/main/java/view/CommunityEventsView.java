package view;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.community.BrowseEventsController;
import interface_adapter.community.CommunityEventsState;
import interface_adapter.community.CommunityEventsViewModel;
import interface_adapter.community.CreateEventController;
import interface_adapter.community.JoinEventController;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Community Events Swing tab: browse events by community, publish new event invitations
 * with capacity limits, and join open events.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CommunityEventsView extends JPanel implements PropertyChangeListener {

    private final CommunityEventsViewModel viewModel;
    private final SessionViewModel sessionViewModel;
    private final JLabel statusLabel =
            new JLabel("TODO [Track B]: Build CommunityEventsView (Browse / Create / Join UI)");

    private BrowseEventsController browseEventsController;
    private CreateEventController createEventController;
    private JoinEventController joinEventController;

    public CommunityEventsView(
            CommunityEventsViewModel viewModel, SessionViewModel sessionViewModel) {
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

    public void setBrowseEventsController(BrowseEventsController browseEventsController) {
        this.browseEventsController = browseEventsController;
    }

    public void setCreateEventController(CreateEventController createEventController) {
        this.createEventController = createEventController;
    }

    public void setJoinEventController(JoinEventController joinEventController) {
        this.joinEventController = joinEventController;
    }

    /**
     * Triggered by {@link MainShellView} when this tab becomes visible.
     */
    public void refreshData() {
        SessionState session = sessionViewModel.getState();
        if (session.isLoggedIn() && browseEventsController != null) {
            browseEventsController.execute(
                    session.getUserId(), viewModel.getState().getSelectedCommunityId());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Runnable updateTask =
                () -> {
                    if (evt.getSource() == sessionViewModel) {
                        SessionState session = (SessionState) evt.getNewValue();
                        if (session != null && !session.isLoggedIn()) {
                            // TODO [Track B]: Reset local form inputs and table rows on logout.
                            statusLabel.setText("Signed out.");
                        }
                        return;
                    }
                    if (evt.getSource() == viewModel) {
                        CommunityEventsState state = (CommunityEventsState) evt.getNewValue();
                        // TODO [Track B]: Render state.getEvents(), join buttons, and form errors.
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
