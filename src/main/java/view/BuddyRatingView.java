package view;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.buddy_rating.BuddyRatingState;
import interface_adapter.buddy_rating.BuddyRatingState.RateableBuddyRow;
import interface_adapter.buddy_rating.BuddyRatingViewModel;
import interface_adapter.buddy_rating.RateBuddyController;
import interface_adapter.buddy_rating.ViewRateableBuddiesController;

/**
 * Post-meetup ratings for buddies who shared a started event with the signed-in user.
 */
public final class BuddyRatingView extends JPanel implements PropertyChangeListener {

    private static final int GAP = 8;
    private static final int MARGIN = 16;
    private final BuddyRatingViewModel viewModel;
    private final SessionViewModel sessionViewModel;
    private final BuddyRatingTable table = new BuddyRatingTable();
    private final JLabel status = new JLabel();
    private final JButton save = new JButton("Save rating");
    private final BuddyRatingForm form = new BuddyRatingForm(this::draftChanged);

    private ViewRateableBuddiesController listController;
    private RateBuddyController rateController;
    private String userId = "";
    private String confirmation = "";
    private boolean busy;
    private boolean draftEdited;

    public BuddyRatingView(BuddyRatingViewModel viewModel, SessionViewModel sessionViewModel) {
        super(new BorderLayout(GAP, GAP));
        this.viewModel = Objects.requireNonNull(viewModel);
        this.sessionViewModel = Objects.requireNonNull(sessionViewModel);
        setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
        final JLabel buddies = new JLabel("Rate a buddy you met at a shared event.");
        buddies.setLabelFor(table);
        add(buddies, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        final JPanel editor = new JPanel(new BorderLayout(GAP, GAP));
        editor.add(form, BorderLayout.CENTER);
        final JPanel actions = new JPanel(new BorderLayout(GAP, GAP));
        actions.add(save, BorderLayout.WEST);
        actions.add(status, BorderLayout.CENTER);
        editor.add(actions, BorderLayout.SOUTH);
        add(editor, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !busy) {
                showSelection();
            }
        });
        save.addActionListener(event -> saveRating());
        viewModel.addPropertyChangeListener(this);
        sessionViewModel.addPropertyChangeListener(this);
        showSelection();
        status.setText("Signed out.");
        updateControls();
    }

    public String getViewName() {
        return viewModel.getViewName();
    }

    /**
     * Connects the list use case.
     * @param controller controller that loads shared-event buddies
     */
    public void setViewRateableBuddiesController(ViewRateableBuddiesController controller) {
        listController = controller;
    }

    /**
     * Connects the rating use case.
     * @param controller controller that saves or updates a rating
     */
    public void setRateBuddyController(RateBuddyController controller) {
        rateController = controller;
    }

    /**
     * Reloads buddies when the shell activates this tab.
     */
    public void refreshData() {
        synchronizeSession();
        if (!userId.isEmpty() && !busy && listController != null) {
            loadRows("");
        }
    }

    private void loadRows(String savedMessage) {
        confirmation = savedMessage;
        final String rater = userId;
        if (savedMessage.isEmpty()) {
            status.setText("Loading buddies...");
        }
        else {
            status.setText(savedMessage);
        }
        startRequest(() -> listController.execute(rater), false);
    }

    private void saveRating() {
        synchronizeSession();
        final RateableBuddyRow selected = table.selectedBuddy();
        if (save.isEnabled() && selected != null) {
            confirmation = "";
            status.setText("Saving rating...");
            startRequest(form.submission(rateController, userId, selected), true);
        }
    }

    private void startRequest(Runnable request, boolean reloadOnSuccess) {
        final BuddyRatingState owner = viewModel.getState();
        busy = true;
        updateControls();
        CompletableFuture.runAsync(viewModel.forCurrentSession(request)).whenComplete((unused, failure) -> {
            SwingUtilities.invokeLater(() -> finishRequest(owner, reloadOnSuccess, failure));
        });
    }

    private void finishRequest(BuddyRatingState owner, boolean reloadOnSuccess, Throwable failure) {
        synchronizeSession();
        if (viewModel.getState() == owner) {
            busy = false;
            if (failure != null) {
                showError("Could not complete the request. Please try again.");
            }
            else if (reloadOnSuccess && owner.getErrorMessage().isEmpty() && listController != null) {
                loadRows(owner.getStatusMessage());
            }
            updateControls();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        final Runnable update = () -> {
            synchronizeSession();
            if (event.getSource() == viewModel && event.getNewValue() == viewModel.getState()) {
                showResult(event.getPropertyName());
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        }
        else {
            SwingUtilities.invokeLater(update);
        }
    }

    private void synchronizeSession() {
        final SessionState session = sessionViewModel.getState();
        String current = "";
        if (session != null && session.isLoggedIn()) {
            current = session.getUserId();
        }
        if (!current.equals(userId)) {
            userId = current;
            viewModel.clearSession();
            confirmation = "";
            busy = false;
            table.replaceRows(Collections.emptyList());
            showSelection();
            status.setText("");
            if (userId.isEmpty()) {
                status.setText("Signed out.");
            }
            updateControls();
        }
    }

    private void showResult(String property) {
        final BuddyRatingState state = viewModel.getState();
        if (BuddyRatingViewModel.ROWS_LOADED.equals(property)) {
            final boolean sameBuddyAndEvent = table.replaceRows(state.getItems());
            if (!sameBuddyAndEvent || !draftEdited || !confirmation.isEmpty()) {
                showSelection();
            }
        }
        if (!state.getErrorMessage().isBlank()) {
            showError(state.getErrorMessage());
        }
        else if (!state.getStatusMessage().isBlank()) {
            status.setText(state.getStatusMessage());
        }
        else {
            status.setText(confirmation);
        }
        updateControls();
    }

    private void showError(String message) {
        status.setText((confirmation + " " + message).trim());
        confirmation = "";
    }

    private void showSelection() {
        form.showRating(table.selectedBuddy());
        draftChanged();
        draftEdited = false;
    }

    private void draftChanged() {
        if (!busy) {
            draftEdited = true;
            confirmation = "";
            status.setText("");
        }
        updateControls();
    }

    private void updateControls() {
        final boolean editable = !busy && !userId.isEmpty() && table.selectedBuddy() != null;
        table.setEnabled(!busy && table.getRowCount() > 0);
        form.enableInputs(editable);
        save.setEnabled(editable && form.hasScore() && rateController != null);
    }
}
