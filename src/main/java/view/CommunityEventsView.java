package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.community.BrowseEventsController;
import interface_adapter.community.CommunityEventsState;
import interface_adapter.community.CommunityEventsState.CommunityOption;
import interface_adapter.community.CommunityEventsState.EventRow;
import interface_adapter.community.CommunityEventsViewModel;
import interface_adapter.community.CreateEventController;
import interface_adapter.community.JoinEventController;

/**
 * Community Events Swing tab: browse events by community, publish new event invitations
 * with capacity limits, and join open events.
 *
 * <p>Layout: a community filter on top, the event table with the selected event's details and
 * Join button in the middle, the "Publish an event" form on the right, and a message line at the
 * bottom. After publishing or joining, the list is reloaded so seat counts are always current.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CommunityEventsView extends JPanel implements PropertyChangeListener {

    private static final String ALL_COMMUNITIES_ID = "ALL";
    private static final String[] COLUMNS = {"When", "Community", "Event", "Organizer", "Spots", "Status"};
    private static final int[] COLUMN_WIDTHS = {130, 80, 220, 90, 55, 70};
    private static final int GAP = 8;
    private static final int OUTER_MARGIN = 12;
    private static final int FORM_WIDTH = 290;
    private static final int TEXT_COLUMNS = 16;
    private static final int DESCRIPTION_ROWS = 3;
    private static final int DETAILS_ROWS = 6;
    private static final int DEFAULT_CAPACITY = 4;
    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 100;
    private static final int DEFAULT_START_HOUR = 19;
    private static final Color SUCCESS_COLOR = new Color(0, 120, 0);
    private static final Color ERROR_COLOR = new Color(180, 0, 0);
    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");
    private static final DateTimeFormatter START_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CommunityEventsViewModel viewModel;
    private final SessionViewModel sessionViewModel;

    private final DefaultComboBoxModel<CommunityOption> filterModel = new DefaultComboBoxModel<>();
    private final JComboBox<CommunityOption> filterCombo = new JComboBox<>(filterModel);
    private final JButton refreshButton = new JButton("Refresh");
    private final JLabel listHintLabel = new JLabel(" ");

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable eventTable = new JTable(tableModel);
    private final JTextArea detailsArea = new JTextArea(DETAILS_ROWS, TEXT_COLUMNS);
    private final JButton joinButton = new JButton("Join event");

    private final DefaultComboBoxModel<CommunityOption> createCommunityModel = new DefaultComboBoxModel<>();
    private final JComboBox<CommunityOption> createCommunityCombo = new JComboBox<>(createCommunityModel);
    private final JTextField titleField = new JTextField(TEXT_COLUMNS);
    private final JTextArea descriptionArea = new JTextArea(DESCRIPTION_ROWS, TEXT_COLUMNS);
    private final JSpinner capacitySpinner =
            new JSpinner(new SpinnerNumberModel(DEFAULT_CAPACITY, MIN_CAPACITY, MAX_CAPACITY, 1));
    private final JTextField startsAtField = new JTextField(TEXT_COLUMNS);
    private final JButton publishButton = new JButton("Publish event");

    private final JLabel feedbackLabel = new JLabel(" ");

    private List<EventRow> rows = Collections.emptyList();
    private List<CommunityOption> knownCommunities = Collections.emptyList();
    private boolean updatingFilter;

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

        setLayout(new BorderLayout(GAP, GAP));
        setBorder(BorderFactory.createEmptyBorder(OUTER_MARGIN, OUTER_MARGIN, OUTER_MARGIN, OUTER_MARGIN));
        add(buildFilterBar(), BorderLayout.NORTH);
        add(buildListPanel(), BorderLayout.CENTER);
        add(buildCreateForm(), BorderLayout.EAST);
        feedbackLabel.setFont(feedbackLabel.getFont().deriveFont(Font.BOLD));
        add(feedbackLabel, BorderLayout.SOUTH);

        filterModel.addElement(allCommunitiesOption());
        resetCreateForm();
        showSelectedEvent();
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
     * Reloads the event list for the selected community.
     * Triggered by {@link MainShellView} when this tab becomes visible, and after publish/join.
     */
    public void refreshData() {
        final SessionState session = sessionViewModel.getState();
        if (session.isLoggedIn() && browseEventsController != null) {
            browseEventsController.execute(session.getUserId(), selectedFilterId());
        }
    }

    private JPanel buildFilterBar() {
        final JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
        bar.add(new JLabel("Community:"));
        bar.add(filterCombo);
        bar.add(refreshButton);
        bar.add(listHintLabel);
        filterCombo.addActionListener(event -> {
            if (!updatingFilter) {
                showFeedback("", false);
                refreshData();
            }
        });
        refreshButton.addActionListener(event -> refreshData());
        return bar;
    }

    private JPanel buildListPanel() {
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.setFillsViewportHeight(true);
        eventTable.getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            eventTable.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
        }
        eventTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedEvent();
            }
        });

        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBackground(getBackground());
        joinButton.addActionListener(event -> joinSelectedEvent());

        final JPanel detailsPanel = new JPanel(new BorderLayout(GAP, GAP));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Selected event"));
        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        final JPanel joinRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        joinRow.add(joinButton);
        detailsPanel.add(joinRow, BorderLayout.SOUTH);

        final JPanel panel = new JPanel(new BorderLayout(GAP, GAP));
        panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);
        panel.add(detailsPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCreateForm() {
        final JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Publish an event"));
        form.setPreferredSize(new Dimension(FORM_WIDTH, 0));
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, GAP / 2, 2, GAP / 2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        startsAtField.setToolTipText("Toronto time, for example 2026-12-03 14:00");

        addFormRow(form, gbc, "Community", createCommunityCombo);
        addFormRow(form, gbc, "Title", titleField);
        addFormRow(form, gbc, "Description (optional)", new JScrollPane(descriptionArea));
        addFormRow(form, gbc, "Max people (" + MIN_CAPACITY + "-" + MAX_CAPACITY + ")", capacitySpinner);
        addFormRow(form, gbc, "Starts at (yyyy-MM-dd HH:mm, Toronto)", startsAtField);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(GAP, GAP / 2, 2, GAP / 2);
        form.add(publishButton, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        form.add(new JLabel(" "), gbc);

        publishButton.addActionListener(event -> publishEvent());
        return form;
    }

    private static void addFormRow(JPanel form, GridBagConstraints gbc, String label, Component field) {
        form.add(new JLabel(label), gbc);
        gbc.gridy++;
        form.add(field, gbc);
        gbc.gridy++;
    }

    private void publishEvent() {
        final SessionState session = sessionViewModel.getState();
        final CommunityOption community = (CommunityOption) createCommunityCombo.getSelectedItem();
        if (createEventController != null && session.isLoggedIn()) {
            createEventController.execute(
                    session.getUserId(),
                    community == null ? "" : community.getCommunityId(),
                    titleField.getText(),
                    descriptionArea.getText(),
                    String.valueOf(capacitySpinner.getValue()),
                    startsAtField.getText());
            refreshData();
        }
    }

    private void joinSelectedEvent() {
        final SessionState session = sessionViewModel.getState();
        final EventRow row = selectedRow();
        if (joinEventController != null && session.isLoggedIn() && row != null) {
            joinEventController.execute(session.getUserId(), row.getEventId());
            refreshData();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final Runnable updateTask = () -> {
            if (evt.getSource() == sessionViewModel) {
                final SessionState session = (SessionState) evt.getNewValue();
                if (session != null && !session.isLoggedIn()) {
                    clearForLogout();
                }
            }
            else if (CommunityEventsViewModel.FEEDBACK_PROPERTY.equals(evt.getPropertyName())) {
                renderFeedback((CommunityEventsState) evt.getNewValue());
            }
            else {
                renderList((CommunityEventsState) evt.getNewValue());
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            updateTask.run();
        }
        else {
            SwingUtilities.invokeLater(updateTask);
        }
    }

    private void renderFeedback(CommunityEventsState state) {
        if (!state.getErrorMessage().isBlank()) {
            showFeedback(state.getErrorMessage(), true);
        }
        else {
            showFeedback(state.getStatusMessage(), false);
        }
        if (state.isClearCreateForm()) {
            resetCreateForm();
        }
    }

    private void renderList(CommunityEventsState state) {
        updateCommunityOptions(state.getCommunities(), state.getSelectedCommunityId());

        final String previouslySelected = selectedRow() == null ? null : selectedRow().getEventId();
        rows = new ArrayList<>(state.getEvents());
        tableModel.setRowCount(0);
        int reselect = -1;
        for (int i = 0; i < rows.size(); i++) {
            final EventRow row = rows.get(i);
            tableModel.addRow(new Object[] {
                row.getStartsAtDisplay(),
                row.getCommunityName(),
                row.getTitle(),
                row.getCreatorDisplayName(),
                row.getJoinedCount() + "/" + row.getCapacity(),
                statusText(row),
            });
            if (row.getEventId().equals(previouslySelected)) {
                reselect = i;
            }
        }
        if (reselect >= 0) {
            eventTable.setRowSelectionInterval(reselect, reselect);
        }
        listHintLabel.setText(rows.isEmpty() ? "No events in this community yet. Publish one!" : rows.size()
                + " event(s)");
        showSelectedEvent();
    }

    /** Refills both community drop-downs when the list of communities changes. */
    private void updateCommunityOptions(List<CommunityOption> communities, String selectedId) {
        updatingFilter = true;
        if (!sameCommunities(communities)) {
            knownCommunities = new ArrayList<>(communities);
            final Object createSelection = createCommunityCombo.getSelectedItem();
            filterModel.removeAllElements();
            filterModel.addElement(allCommunitiesOption());
            createCommunityModel.removeAllElements();
            for (CommunityOption option : communities) {
                filterModel.addElement(option);
                createCommunityModel.addElement(option);
            }
            selectById(createCommunityCombo, createCommunityModel,
                    createSelection == null ? "" : ((CommunityOption) createSelection).getCommunityId());
        }
        selectById(filterCombo, filterModel, selectedId);
        updatingFilter = false;
    }

    private boolean sameCommunities(List<CommunityOption> communities) {
        boolean same = communities.size() == knownCommunities.size();
        for (int i = 0; same && i < communities.size(); i++) {
            same = communities.get(i).getCommunityId().equals(knownCommunities.get(i).getCommunityId());
        }
        return same;
    }

    private static void selectById(
            JComboBox<CommunityOption> combo, DefaultComboBoxModel<CommunityOption> model, String communityId) {
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).getCommunityId().equalsIgnoreCase(communityId)) {
                combo.setSelectedIndex(i);
            }
        }
    }

    private void showSelectedEvent() {
        final EventRow row = selectedRow();
        if (row == null) {
            detailsArea.setText("Select an event to see its details.");
            joinButton.setText("Join event");
            joinButton.setEnabled(false);
        }
        else {
            final String description = row.getDescription().isBlank() ? "(no description)" : row.getDescription();
            detailsArea.setText(row.getTitle() + "  -  " + row.getCommunityName() + "\n"
                    + row.getStartsAtDisplay() + " (Toronto time), organized by " + row.getCreatorDisplayName()
                    + "\n" + row.getJoinedCount() + " of " + row.getCapacity() + " spots taken\n\n"
                    + description);
            detailsArea.setCaretPosition(0);
            joinButton.setText(joinButtonText(row));
            joinButton.setEnabled(canJoin(row));
        }
    }

    private static boolean canJoin(EventRow row) {
        return row.isOpenForJoin() && !row.isJoinedByCurrentUser() && !row.isFull();
    }

    private static String joinButtonText(EventRow row) {
        final String text;
        if (row.isJoinedByCurrentUser()) {
            text = "You're going";
        }
        else if (!row.isOpenForJoin()) {
            text = "Already started";
        }
        else if (row.isFull()) {
            text = "Event is full";
        }
        else {
            text = "Join event";
        }
        return text;
    }

    private static String statusText(EventRow row) {
        final String text;
        if (row.isJoinedByCurrentUser()) {
            text = "Joined";
        }
        else if (!row.isOpenForJoin()) {
            text = "Started";
        }
        else if (row.isFull()) {
            text = "Full";
        }
        else {
            text = "Open";
        }
        return text;
    }

    private EventRow selectedRow() {
        final int index = eventTable.getSelectedRow();
        EventRow row = null;
        if (index >= 0 && index < rows.size()) {
            row = rows.get(index);
        }
        return row;
    }

    private String selectedFilterId() {
        final CommunityOption selected = (CommunityOption) filterCombo.getSelectedItem();
        return selected == null ? ALL_COMMUNITIES_ID : selected.getCommunityId();
    }

    private void showFeedback(String message, boolean error) {
        feedbackLabel.setForeground(error ? ERROR_COLOR : SUCCESS_COLOR);
        feedbackLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    private void resetCreateForm() {
        titleField.setText("");
        descriptionArea.setText("");
        capacitySpinner.setValue(DEFAULT_CAPACITY);
        startsAtField.setText(LocalDate.now(TORONTO).plusDays(1).atTime(LocalTime.of(DEFAULT_START_HOUR, 0))
                .format(START_FORMAT));
    }

    private void clearForLogout() {
        rows = Collections.emptyList();
        tableModel.setRowCount(0);
        updatingFilter = true;
        filterCombo.setSelectedIndex(0);
        updatingFilter = false;
        listHintLabel.setText(" ");
        showFeedback("", false);
        resetCreateForm();
        showSelectedEvent();
    }

    private static CommunityOption allCommunitiesOption() {
        return new CommunityOption(ALL_COMMUNITIES_ID, "All communities");
    }
}
