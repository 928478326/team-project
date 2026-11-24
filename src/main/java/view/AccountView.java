package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import interface_adapter.account.AccountState;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.LoginController;
import interface_adapter.account.SignupController;

/**
 * Login &amp; Registration Swing view.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class AccountView extends JPanel implements PropertyChangeListener {

    private final AccountViewModel viewModel;
    private final JTextField loginUsernameField = new JTextField(18);
    private final JPasswordField loginPasswordField = new JPasswordField(18);
    private final JTextField signupUsernameField = new JTextField(18);
    private final JPasswordField signupPasswordField = new JPasswordField(18);
    private final JPasswordField signupConfirmField = new JPasswordField(18);
    private final JButton loginButton = new JButton("Log In");
    private final JButton signupButton = new JButton("Create Account");
    private final JLabel feedbackLabel = new JLabel(" ");

    private LoginController loginController;
    private SignupController signupController;

    public AccountView(AccountViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        final JLabel title = new JLabel("CampusBuddy — UofT Student Buddy Finder", JLabel.CENTER);
        add(title, BorderLayout.NORTH);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", buildLoginPanel());
        tabs.addTab("Sign Up", buildSignupPanel());
        add(tabs, BorderLayout.CENTER);

        feedbackLabel.setHorizontalAlignment(JLabel.CENTER);
        add(feedbackLabel, BorderLayout.SOUTH);
    }

    public String getViewName() {
        return viewModel.getViewName();
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }

    public void setSignupController(SignupController signupController) {
        this.signupController = signupController;
    }

    private JPanel buildLoginPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(loginUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(loginPasswordField, gbc);

        loginButton.addActionListener(
                e -> {
                    if (loginController != null) {
                        final String username = loginUsernameField.getText();
                        final String password = new String(loginPasswordField.getPassword());
                        submit(() -> loginController.execute(username, password));
                    }
                });
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(loginButton, gbc);
        return panel;
    }

    private JPanel buildSignupPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(signupUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(signupPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        panel.add(signupConfirmField, gbc);

        signupButton.addActionListener(
                e -> {
                    if (signupController != null) {
                        final String username = signupUsernameField.getText();
                        final String password = new String(signupPasswordField.getPassword());
                        final String confirmation = new String(signupConfirmField.getPassword());
                        submit(() -> signupController.execute(username, password, confirmation));
                    }
                });
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(signupButton, gbc);
        return panel;
    }

    private void submit(Runnable request) {
        loginButton.setEnabled(false);
        signupButton.setEnabled(false);
        CompletableFuture.runAsync(request).whenComplete((unused, error) -> {
            SwingUtilities.invokeLater(() -> {
                loginButton.setEnabled(true);
                signupButton.setEnabled(true);
                if (error != null) {
                    feedbackLabel.setForeground(Color.RED);
                    feedbackLabel.setText("Could not complete the request. Please try again.");
                }
            });
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!AccountViewModel.STATE_PROPERTY.equals(evt.getPropertyName())) {
            return;
        }
        final AccountState state = (AccountState) evt.getNewValue();
        final Runnable updateTask = () -> showFeedback(state);
        if (SwingUtilities.isEventDispatchThread()) {
            updateTask.run();
        } else {
            SwingUtilities.invokeLater(updateTask);
        }
    }

    private void showFeedback(AccountState state) {
        if (!state.getErrorMessage().isBlank()) {
            feedbackLabel.setForeground(Color.RED);
            feedbackLabel.setText(state.getErrorMessage());
            return;
        }
        loginPasswordField.setText("");
        signupPasswordField.setText("");
        signupConfirmField.setText("");
        feedbackLabel.setForeground(new Color(0, 120, 0));
        feedbackLabel.setText(state.getStatusMessage().isEmpty() ? " " : state.getStatusMessage());
    }
}
