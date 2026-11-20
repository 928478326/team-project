package view;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.recommendation.EditProfileController;
import interface_adapter.recommendation.RecommendBuddiesController;
import interface_adapter.recommendation.RecommendationState;
import interface_adapter.recommendation.RecommendationViewModel;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Profile &amp; 3+4 Intelligent Buddy Recommendation Swing tab: edits display name, contact info,
 * weekend hangout style, and interest communities, and displays Top-K recommendations with
 * score breakdowns and LLM-generated explanations/icebreakers.
 *
 * <p><b>Owner: Track D (Profile, 3+4 Recommendation &amp; LLM Lead)</b>
 *
 * <p>Note: Execute LLM recommendation calls inside a {@link javax.swing.SwingWorker} in this View
 * so the Swing EDT stays responsive while awaiting remote HTTP responses.
 */
public final class ProfileAndRecommendView extends JPanel implements PropertyChangeListener {

    private final RecommendationViewModel viewModel;
    private final SessionViewModel sessionViewModel;
    private final JLabel statusLabel =
            new JLabel("TODO [Track D]: Build ProfileAndRecommendView (Profile Form + 3+4 AI Cards)");

    private EditProfileController editProfileController;
    private RecommendBuddiesController recommendBuddiesController;

    public ProfileAndRecommendView(
            RecommendationViewModel viewModel, SessionViewModel sessionViewModel) {
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

    public void setEditProfileController(EditProfileController editProfileController) {
        this.editProfileController = editProfileController;
    }

    public void setRecommendBuddiesController(
            RecommendBuddiesController recommendBuddiesController) {
        this.recommendBuddiesController = recommendBuddiesController;
    }

    /**
     * Triggered by {@link MainShellView} when this tab becomes visible.
     */
    public void refreshData() {
        SessionState session = sessionViewModel.getState();
        if (session.isLoggedIn() && editProfileController != null) {
            editProfileController.loadProfile(session.getUserId());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Runnable updateTask =
                () -> {
                    if (evt.getSource() == sessionViewModel) {
                        SessionState session = (SessionState) evt.getNewValue();
                        if (session != null && !session.isLoggedIn()) {
                            // TODO [Track D]: Clear profile inputs & recommendation cards on logout.
                            statusLabel.setText("Signed out.");
                        }
                        return;
                    }
                    if (evt.getSource() == viewModel) {
                        RecommendationState state = (RecommendationState) evt.getNewValue();
                        // TODO [Track D]: Populate profile form + render 3+4 recommendation cards.
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
