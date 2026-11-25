package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import interface_adapter.SessionViewModel;
import interface_adapter.recommendation.EditProfileController;
import interface_adapter.recommendation.RecommendBuddiesController;
import interface_adapter.recommendation.RecommendationState;
import interface_adapter.recommendation.RecommendationViewModel;

/** Signed-in profile editor with an owner-only settings section and a saved match card preview. */
public final class ProfileAndRecommendView extends JPanel implements PropertyChangeListener {
    private static final Color ERROR = new Color(0x9B302C);
    private static final double EDITOR_WEIGHT = 0.62;
    private final RecommendationViewModel viewModel;
    private final SessionViewModel sessionViewModel;
    private final ProfileEditorPanel editor = new ProfileEditorPanel();
    private final ProfilePreviewPanel preview = new ProfilePreviewPanel();
    private final JButton save = new JButton("Save Profile");
    private final JButton retry = new JButton("Retry load");
    private final JLabel feedback = new JLabel(" ");
    private final Set<String> savingOwners = new HashSet<>();
    private EditProfileController editProfileController;
    private RecommendBuddiesController recommendBuddiesController;
    private String owner = "";
    private boolean loaded;

    public ProfileAndRecommendView(RecommendationViewModel viewModel, SessionViewModel sessionViewModel) {
        super(new BorderLayout(ProfileLayout.PAD, ProfileLayout.GAP));
        this.viewModel = Objects.requireNonNull(viewModel);
        this.sessionViewModel = Objects.requireNonNull(sessionViewModel);
        viewModel.addPropertyChangeListener(this);
        sessionViewModel.addPropertyChangeListener(this);
        setBackground(ProfileLayout.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(ProfileLayout.PAD, ProfileLayout.PAD,
                ProfileLayout.GAP, ProfileLayout.PAD));
        add(ProfileLayout.heading("My profile", ProfileLayout.TITLE), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
        clearProfile();
    }

    private JScrollPane buildBody() {
        final JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);
        final GridBagConstraints cell = new GridBagConstraints();
        cell.gridy = 0;
        cell.anchor = GridBagConstraints.NORTHWEST;
        cell.fill = GridBagConstraints.HORIZONTAL;
        cell.weightx = EDITOR_WEIGHT;
        columns.add(editor, cell);
        cell.insets.left = ProfileLayout.PAD;
        cell.weightx = 1 - EDITOR_WEIGHT;
        columns.add(preview, cell);
        final JPanel top = new JPanel(new BorderLayout());
        top.setBackground(ProfileLayout.CANVAS);
        top.add(columns, BorderLayout.NORTH);
        final JScrollPane scroll = new JScrollPane(top);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(ProfileLayout.PAD);
        return scroll;
    }

    private JPanel buildActions() {
        final JPanel actions = new JPanel(new BorderLayout(ProfileLayout.GAP, 0));
        actions.setOpaque(false);
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
        buttons.setOpaque(false);
        retry.addActionListener(event -> refreshData());
        save.addActionListener(event -> saveProfile());
        save.setForeground(Color.WHITE);
        save.setBackground(ProfileLayout.TEAL);
        save.setContentAreaFilled(false);
        save.setOpaque(true);
        buttons.add(retry);
        buttons.add(save);
        feedback.putClientProperty("html.disable", true);
        actions.add(feedback, BorderLayout.CENTER);
        actions.add(buttons, BorderLayout.EAST);
        return actions;
    }

    public String getViewName() {
        return viewModel.getViewName();
    }

    public void setEditProfileController(EditProfileController controller) {
        editProfileController = controller;
    }

    public void setRecommendBuddiesController(RecommendBuddiesController controller) {
        recommendBuddiesController = controller;
    }

    /** Loads the active account on tab entry and keeps unsaved edits when switching between tabs. */
    public void refreshData() {
        syncOwner();
        if (!owner.isEmpty() && !loaded && !savingOwners.contains(owner) && editProfileController != null) {
            editProfileController.loadProfile(owner);
        }
    }

    private void syncOwner() {
        final String current = sessionViewModel.getState().getUserId();
        if (!owner.equals(current)) {
            owner = current;
            clearProfile();
        }
    }

    private void clearProfile() {
        loaded = false;
        editor.clear();
        preview.clear();
        retry.setVisible(false);
        message(" ", false);
        updateControls();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (SwingUtilities.isEventDispatchThread()) {
            applyEvent(event);
        }
        else {
            SwingUtilities.invokeLater(() -> applyEvent(event));
        }
    }

    private void applyEvent(PropertyChangeEvent event) {
        syncOwner();
        if (event.getSource() == viewModel && !owner.isEmpty() && event.getNewValue() == viewModel.getState()) {
            final RecommendationState state = viewModel.getState();
            if (!state.getErrorMessage().isEmpty()) {
                message(state.getErrorMessage(), true);
                retry.setVisible(!loaded);
            }
            else if (!state.getDisplayName().isEmpty()) {
                editor.populate(state);
                preview.populate(state);
                loaded = true;
                retry.setVisible(false);
                message(state.getStatusMessage(), false);
            }
            updateControls();
        }
    }

    private void updateControls() {
        final boolean editable = loaded && !savingOwners.contains(owner);
        editor.allowEditing(editable);
        save.setEnabled(editable);
    }

    private void message(String text, boolean error) {
        feedback.setText(text);
        if (error) {
            feedback.setForeground(ERROR);
        }
        else {
            feedback.setForeground(ProfileLayout.TEAL);
        }
    }

    private void saveProfile() {
        final String submittedOwner = owner;
        final Runnable submit = editor.captureSave(editProfileController, submittedOwner);
        savingOwners.add(submittedOwner);
        updateControls();
        message("Saving profile...", false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                submit.run();
                return null;
            }

            @Override
            protected void done() {
                savingOwners.remove(submittedOwner);
                if (submittedOwner.equals(owner)) {
                    try {
                        get();
                        if (!loaded) {
                            refreshData();
                        }
                    }
                    catch (ExecutionException ex) {
                        message("Could not save the profile. Please try again.", true);
                    }
                    catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        message("Save interrupted. Please try again.", true);
                    }
                    updateControls();
                }
            }
        }.execute();
    }
}
