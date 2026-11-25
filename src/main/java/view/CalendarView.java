package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.calendar.CalendarState;
import interface_adapter.calendar.CalendarState.CalendarEventRow;
import interface_adapter.calendar.CalendarState.HolidayRow;
import interface_adapter.calendar.CalendarViewModel;
import interface_adapter.calendar.ViewCalendarController;

/**
 * Personal Monthly Calendar Swing tab: displays joined events in Toronto local time,
 * Canadian/Ontario public holidays, and supports exporting to {@code .ics}.
 *
 * <p>Layout: month navigation and the export button on top, a Sunday-first month grid in the
 * middle, and a list of the month's events and holidays on the right.
 *
 * <p>Every load runs through a {@link SwingWorker} because it may call the remote holiday API;
 * the buttons are disabled meanwhile so only one request is in flight. When a different student
 * signs in, the previous student's request is abandoned: the controls unlock straight away and
 * only a state owned by the signed-in student is ever drawn.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CalendarView extends JPanel implements PropertyChangeListener {

    private static final int DAYS_PER_WEEK = 7;
    private static final int GAP = 8;
    private static final int CELL_GAP = 2;
    private static final int OUTER_MARGIN = 12;
    private static final int CELL_PADDING = 3;
    private static final int CELL_TEXT_WIDTH = 76;
    private static final int MONTH_LABEL_WIDTH = 200;
    private static final int AGENDA_ROWS = 20;
    private static final int AGENDA_COLUMNS = 20;
    private static final int AGENDA_WIDTH = 260;
    private static final float MONTH_FONT_SIZE = 18f;
    private static final Color HOLIDAY_COLOR = new Color(176, 0, 32);
    private static final Color EVENT_COLOR = new Color(0, 70, 140);
    private static final Color GRID_LINE_COLOR = new Color(205, 205, 205);
    private static final Color TODAY_COLOR = new Color(0, 90, 200);
    private static final Color OUTSIDE_MONTH_COLOR = new Color(245, 245, 245);
    private static final Color ERROR_COLOR = new Color(180, 0, 0);
    private static final Color SUCCESS_COLOR = new Color(0, 120, 0);
    /** A single space keeps an empty label at its normal height. */
    private static final String BLANK_LABEL = " ";
    private static final DateTimeFormatter AGENDA_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE MMM d", Locale.ENGLISH);

    private final CalendarViewModel viewModel;
    private final SessionViewModel sessionViewModel;

    private final JButton previousButton = new JButton("< Prev");
    private final JButton nextButton = new JButton("Next >");
    private final JButton thisMonthButton = new JButton("This month");
    private final JButton exportButton = new JButton("Export month to .ics...");
    private final JLabel monthLabel = new JLabel(BLANK_LABEL, SwingConstants.CENTER);
    private final JPanel weekdayHeader = new JPanel(new GridLayout(1, DAYS_PER_WEEK, CELL_GAP, 0));
    private final JPanel gridPanel = new JPanel(new GridLayout(0, DAYS_PER_WEEK, CELL_GAP, CELL_GAP));
    private final JTextArea agendaArea = new JTextArea(AGENDA_ROWS, AGENDA_COLUMNS);
    private final JLabel zoneLabel = new JLabel(BLANK_LABEL);
    private final JLabel statusLabel = new JLabel(BLANK_LABEL);

    private ViewCalendarController viewCalendarController;
    private boolean loading;
    /** Bumped on every sign-in change so a request from the previous student cannot unlock the UI. */
    private int requestGeneration;
    /** The student the calendar is currently drawn for, or {@code ""} when signed out. */
    private String shownUserId = "";

    public CalendarView(CalendarViewModel viewModel, SessionViewModel sessionViewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.sessionViewModel =
                Objects.requireNonNull(sessionViewModel, "sessionViewModel must not be null");
        this.viewModel.addPropertyChangeListener(this);
        this.sessionViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout(GAP, GAP));
        setBorder(BorderFactory.createEmptyBorder(OUTER_MARGIN, OUTER_MARGIN, OUTER_MARGIN, OUTER_MARGIN));
        add(buildNavigationBar(), BorderLayout.NORTH);
        add(buildMonthPanel(), BorderLayout.CENTER);
        add(buildAgendaPanel(), BorderLayout.EAST);
        add(buildStatusBar(), BorderLayout.SOUTH);

        final SessionState session = sessionViewModel.getState();
        if (session != null && session.isLoggedIn()) {
            shownUserId = session.getUserId();
        }
        render(viewModel.getState(), false);
    }

    public String getViewName() {
        return viewModel.getViewName();
    }

    public void setViewCalendarController(ViewCalendarController viewCalendarController) {
        this.viewCalendarController = viewCalendarController;
    }

    /**
     * Reloads the displayed month. Triggered by {@link MainShellView} when this tab becomes visible.
     */
    public void refreshData() {
        final CalendarState state = viewModel.getState();
        loadMonth(YearMonth.of(state.getYear(), state.getMonth()));
    }

    private JPanel buildNavigationBar() {
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, MONTH_FONT_SIZE));
        // Fixed width so the buttons do not shift when the month name changes length.
        monthLabel.setText("September 2026");
        monthLabel.setPreferredSize(new Dimension(MONTH_LABEL_WIDTH, monthLabel.getPreferredSize().height));

        final JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
        left.add(previousButton);
        left.add(monthLabel);
        left.add(nextButton);
        left.add(thisMonthButton);

        final JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, 0));
        right.add(exportButton);

        previousButton.addActionListener(event -> loadMonth(displayedMonth().minusMonths(1)));
        nextButton.addActionListener(event -> loadMonth(displayedMonth().plusMonths(1)));
        thisMonthButton.addActionListener(event -> loadMonth(YearMonth.now(displayZone())));
        exportButton.addActionListener(event -> chooseFileAndExport());

        final JPanel bar = new JPanel(new BorderLayout());
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildMonthPanel() {
        for (int i = 0; i < DAYS_PER_WEEK; i++) {
            final DayOfWeek day = DayOfWeek.SUNDAY.plus(i);
            final JLabel header = new JLabel(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    SwingConstants.CENTER);
            header.setFont(header.getFont().deriveFont(Font.BOLD));
            weekdayHeader.add(header);
        }
        gridPanel.setBackground(GRID_LINE_COLOR);
        gridPanel.setBorder(BorderFactory.createLineBorder(GRID_LINE_COLOR));

        final JPanel panel = new JPanel(new BorderLayout(0, CELL_GAP));
        panel.add(weekdayHeader, BorderLayout.NORTH);
        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAgendaPanel() {
        agendaArea.setEditable(false);
        agendaArea.setLineWrap(true);
        agendaArea.setWrapStyleWord(true);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("This month"));
        panel.setPreferredSize(new Dimension(AGENDA_WIDTH, 0));
        panel.add(new JScrollPane(agendaArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusBar() {
        final JPanel bar = new JPanel(new BorderLayout());
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(zoneLabel, BorderLayout.EAST);
        return bar;
    }

    private void loadMonth(YearMonth month) {
        final SessionState session = sessionViewModel.getState();
        if (session.isLoggedIn() && viewCalendarController != null && !loading) {
            runInBackground("Loading " + monthTitle(month) + "...",
                () -> viewCalendarController.loadMonth(session.getUserId(), month.getYear(), month.getMonthValue()));
        }
    }

    private void chooseFileAndExport() {
        final SessionState session = sessionViewModel.getState();
        final YearMonth month = displayedMonth();
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export " + monthTitle(month) + " to calendar file");
        chooser.setFileFilter(new FileNameExtensionFilter("iCalendar file (*.ics)", "ics"));
        chooser.setSelectedFile(new File(String.format("campusbuddy-%d-%02d.ics", month.getYear(),
                month.getMonthValue())));
        if (session.isLoggedIn() && viewCalendarController != null && !loading
                && chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase(Locale.ROOT).endsWith(".ics")) {
                target = new File(target.getParentFile(), target.getName() + ".ics");
            }
            final boolean confirmed = !target.exists() || JOptionPane.showConfirmDialog(this,
                    target.getName() + " already exists. Replace it?", "Replace file",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            if (confirmed) {
                final String path = target.getAbsolutePath();
                runInBackground("Exporting...", () -> {
                    viewCalendarController.exportMonth(
                            session.getUserId(), month.getYear(), month.getMonthValue(), path);
                });
            }
        }
    }

    /**
     * Runs a controller call off the Event Dispatch Thread. The presenter's update reaches
     * {@link #propertyChange} on the worker thread and is re-posted to the EDT there.
     *
     * @param message status text shown while the request runs
     * @param controllerCall the controller call to run in the background
     */
    private void runInBackground(String message, Runnable controllerCall) {
        final int generation = requestGeneration;
        setLoading(true);
        showStatus(message, false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                controllerCall.run();
                return null;
            }

            @Override
            protected void done() {
                if (generation == requestGeneration) {
                    setLoading(false);
                }
            }
        }.execute();
    }

    private void setLoading(boolean value) {
        loading = value;
        previousButton.setEnabled(!value);
        nextButton.setEnabled(!value);
        thisMonthButton.setEnabled(!value);
        exportButton.setEnabled(!value);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SwingUtilities.isEventDispatchThread()) {
            handleChange(evt);
        }
        else {
            SwingUtilities.invokeLater(() -> handleChange(evt));
        }
    }

    private void handleChange(PropertyChangeEvent evt) {
        if (evt.getSource() == sessionViewModel) {
            onSessionChanged((SessionState) evt.getNewValue());
        }
        else if (evt.getSource() == viewModel) {
            final CalendarState state = (CalendarState) evt.getNewValue();
            // Draw only a calendar that belongs to the student who is signed in right now.
            if (!shownUserId.isEmpty() && shownUserId.equals(state.getOwnerUserId())) {
                render(state, true);
            }
        }
    }

    /**
     * Clears the calendar whenever the signed-in student changes (logout, or a different login).
     *
     * @param session the new session state
     */
    private void onSessionChanged(SessionState session) {
        final String userId;
        if (session != null && session.isLoggedIn()) {
            userId = session.getUserId();
        }
        else {
            userId = "";
        }
        if (!userId.equals(shownUserId)) {
            shownUserId = userId;
            requestGeneration++;
            setLoading(false);
            render(viewModel.getState(), false);
            showStatus("", false);
        }
    }

    /**
     * Redraws the header, grid, and agenda.
     *
     * @param state latest calendar state
     * @param showData {@code false} draws an empty month (before sign-in or after logout)
     */
    private void render(CalendarState state, boolean showData) {
        final YearMonth month = YearMonth.of(state.getYear(), state.getMonth());
        List<CalendarEventRow> events = new ArrayList<>();
        List<HolidayRow> holidays = new ArrayList<>();
        if (showData) {
            events = state.getEvents();
            holidays = state.getHolidays();
        }

        monthLabel.setText(monthTitle(month));
        zoneLabel.setText("Times shown in " + state.getZoneLabel());
        renderGrid(month, events, holidays);
        renderAgenda(month, events, holidays);
        if (showData) {
            if (!state.getErrorMessage().isBlank()) {
                showStatus(state.getErrorMessage(), true);
            }
            else if (!state.getStatusMessage().isBlank()) {
                showStatus(state.getStatusMessage(), false);
            }
            else if (events.isEmpty()) {
                showStatus("No joined events this month.", false);
            }
            else {
                showStatus("", false);
            }
        }
    }

    private void renderGrid(YearMonth month, List<CalendarEventRow> events, List<HolidayRow> holidays) {
        gridPanel.removeAll();
        // DayOfWeek values run Monday=1..Sunday=7; the grid starts on Sunday.
        final int leadingBlanks = month.atDay(1).getDayOfWeek().getValue() % DAYS_PER_WEEK;
        final int usedCells = leadingBlanks + month.lengthOfMonth();
        final int totalCells = (usedCells + DAYS_PER_WEEK - 1) / DAYS_PER_WEEK * DAYS_PER_WEEK;
        final LocalDate today = LocalDate.now(displayZone());
        for (int cell = 0; cell < totalCells; cell++) {
            final int day = cell - leadingBlanks + 1;
            if (day >= 1 && day <= month.lengthOfMonth()) {
                gridPanel.add(dayCell(day, month.atDay(day).equals(today), events, holidays));
            }
            else {
                final JPanel blank = new JPanel();
                blank.setBackground(OUTSIDE_MONTH_COLOR);
                gridPanel.add(blank);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private static JPanel dayCell(int day, boolean isToday, List<CalendarEventRow> events, List<HolidayRow> holidays) {
        final JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(Color.WHITE);
        cell.setBorder(dayBorder(isToday));

        final JLabel number = new JLabel(String.valueOf(day));
        number.setFont(number.getFont().deriveFont(Font.BOLD));
        if (isToday) {
            number.setForeground(TODAY_COLOR);
        }
        cell.add(number, BorderLayout.NORTH);

        final StringBuilder html = new StringBuilder("<html><div style='width:" + CELL_TEXT_WIDTH + "px'>");
        final StringBuilder tooltip = new StringBuilder();
        for (HolidayRow holiday : holidays) {
            if (holiday.getDayOfMonth() == day) {
                html.append("<font color='").append(hex(HOLIDAY_COLOR)).append("'>")
                        .append(escapeHtml(holiday.getName())).append("</font><br>");
                tooltip.append(holiday.getName()).append(" (holiday)  ");
            }
        }
        for (CalendarEventRow event : events) {
            if (event.getDayOfMonth() == day) {
                html.append("<font color='").append(hex(EVENT_COLOR)).append("'>")
                        .append(escapeHtml(event.getTimeText() + ' ' + event.getTitle())).append("</font><br>");
                tooltip.append(event.getTimeText()).append(' ').append(event.getTitle()).append("  ");
            }
        }
        html.append("</div></html>");
        final JLabel content = new JLabel(html.toString());
        content.setVerticalAlignment(SwingConstants.TOP);
        cell.add(content, BorderLayout.CENTER);
        if (tooltip.length() > 0) {
            cell.setToolTipText(tooltip.toString().trim());
            content.setToolTipText(tooltip.toString().trim());
        }
        return cell;
    }

    private void renderAgenda(YearMonth month, List<CalendarEventRow> events, List<HolidayRow> holidays) {
        final StringBuilder text = new StringBuilder();
        if (!events.isEmpty()) {
            text.append("YOUR EVENTS\n");
            for (CalendarEventRow event : events) {
                text.append(event.getFormattedDateTime()).append('\n')
                        .append("   ").append(event.getTitle())
                        .append(" (").append(event.getCommunityName()).append(")\n");
            }
        }
        if (!holidays.isEmpty()) {
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append("ONTARIO HOLIDAYS\n");
            for (HolidayRow holiday : holidays) {
                text.append(month.atDay(holiday.getDayOfMonth()).format(AGENDA_DATE_FORMAT))
                        .append("  ").append(holiday.getName()).append('\n');
            }
        }
        if (text.length() == 0) {
            text.append("Nothing scheduled.");
        }
        agendaArea.setText(text.toString());
        agendaArea.setCaretPosition(0);
    }

    private YearMonth displayedMonth() {
        final CalendarState state = viewModel.getState();
        return YearMonth.of(state.getYear(), state.getMonth());
    }

    private ZoneId displayZone() {
        ZoneId zone;
        try {
            zone = ZoneId.of(viewModel.getState().getZoneLabel());
        }
        catch (DateTimeException ex) {
            zone = ZoneId.of(CalendarState.DEFAULT_ZONE);
        }
        return zone;
    }

    private void showStatus(String message, boolean error) {
        if (error) {
            statusLabel.setForeground(ERROR_COLOR);
        }
        else {
            statusLabel.setForeground(SUCCESS_COLOR);
        }
        if (message == null || message.isBlank()) {
            statusLabel.setText(BLANK_LABEL);
        }
        else {
            statusLabel.setText(message);
        }
    }

    /**
     * Builds the cell border: plain padding, or a blue outline around today's cell.
     *
     * @param isToday whether the cell shows today's date
     * @return the border for the cell
     */
    private static Border dayBorder(boolean isToday) {
        final Border border;
        if (isToday) {
            final int inner = CELL_PADDING - 2;
            border = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TODAY_COLOR, 2),
                    BorderFactory.createEmptyBorder(inner, inner, inner, inner));
        }
        else {
            border = BorderFactory.createEmptyBorder(CELL_PADDING, CELL_PADDING, CELL_PADDING, CELL_PADDING);
        }
        return border;
    }

    private static String monthTitle(YearMonth month) {
        return month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ' ' + month.getYear();
    }

    private static String hex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
