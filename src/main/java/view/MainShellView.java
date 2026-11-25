package view;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.ViewManagerModel;
import interface_adapter.account.LogoutController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Shared post-login navigation shell hosting all feature tabs in a central {@link CardLayout}.
 *
 * <p>Open/Closed by design: feature tabs register via
 * {@link #addFeatureTab(String, String, JPanel, Runnable)} so adding or modifying a feature view
 * does not require editing this shell.
 */
public final class MainShellView extends JPanel implements PropertyChangeListener {

    private final SessionViewModel sessionViewModel;
    private final JPanel tabButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);
    private final JLabel userLabel = new JLabel("Not signed in");
    private final JButton logoutButton = new JButton("Logout");
    private final Map<String, Runnable> tabRefreshCallbacks = new LinkedHashMap<>();

    private LogoutController logoutController;
    private String activeTabKey;

    public MainShellView(SessionViewModel sessionViewModel) {
        this.sessionViewModel =
                Objects.requireNonNull(sessionViewModel, "sessionViewModel must not be null");
        this.sessionViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titleLabel = new JLabel("CampusBuddy");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHeader.add(titleLabel);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightHeader.add(userLabel);
        rightHeader.add(logoutButton);

        headerPanel.add(leftHeader, BorderLayout.CENTER);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        // The tab buttons get a full-width row of their own. Sharing a FlowLayout row with the title
        // made them wrap onto a second line that the header's one-row height then cut off.
        headerPanel.add(tabButtonsPanel, BorderLayout.SOUTH);

        logoutButton.addActionListener(
                e -> {
                    if (logoutController != null) {
                        logoutController.execute(sessionViewModel.getState().getUsername());
                    }
                });

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        refreshSessionHeader(sessionViewModel.getState());
    }

    public String getViewName() {
        return ViewManagerModel.MAIN_SHELL_VIEW;
    }

    public void setLogoutController(LogoutController logoutController) {
        this.logoutController = logoutController;
    }

    /**
     * Registers a feature panel as a top-level navigation tab (OCP).
     *
     * @param tabKey unique card key
     * @param tabLabel button caption in the navigation bar
     * @param panel feature Swing panel
     * @param onActivated callback invoked when the user switches to this tab (may be null)
     */
    public void addFeatureTab(
            String tabKey, String tabLabel, JPanel panel, Runnable onActivated) {
        Objects.requireNonNull(tabKey, "tabKey must not be null");
        Objects.requireNonNull(tabLabel, "tabLabel must not be null");
        Objects.requireNonNull(panel, "panel must not be null");

        contentPanel.add(panel, tabKey);
        tabRefreshCallbacks.put(tabKey, onActivated == null ? () -> {} : onActivated);

        JButton tabButton = new JButton(tabLabel);
        tabButton.addActionListener(e -> showTab(tabKey));
        tabButtonsPanel.add(tabButton);

        if (activeTabKey == null) {
            activeTabKey = tabKey;
            contentLayout.show(contentPanel, tabKey);
        }
        revalidate();
        repaint();
    }

    /**
     * Activates a registered tab and invokes its refresh callback if signed in.
     *
     * @param tabKey registered tab key
     */
    public void showTab(String tabKey) {
        if (!tabRefreshCallbacks.containsKey(tabKey)) {
            return;
        }
        activeTabKey = tabKey;
        contentLayout.show(contentPanel, tabKey);
        if (sessionViewModel.getState().isLoggedIn()) {
            tabRefreshCallbacks.get(tabKey).run();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!SessionViewModel.STATE_PROPERTY.equals(evt.getPropertyName())) {
            return;
        }
        SessionState sessionState = (SessionState) evt.getNewValue();
        Runnable updateTask =
                () -> {
                    refreshSessionHeader(sessionState);
                    if (sessionState != null && sessionState.isLoggedIn() && activeTabKey != null) {
                        tabRefreshCallbacks.get(activeTabKey).run();
                    }
                };
        if (SwingUtilities.isEventDispatchThread()) {
            updateTask.run();
        } else {
            SwingUtilities.invokeLater(updateTask);
        }
    }

    private void refreshSessionHeader(SessionState state) {
        if (state != null && state.isLoggedIn()) {
            userLabel.setText("Logged in: " + state.getDisplayName());
            logoutButton.setEnabled(true);
        } else {
            userLabel.setText("Not signed in");
            logoutButton.setEnabled(false);
        }
    }
}
