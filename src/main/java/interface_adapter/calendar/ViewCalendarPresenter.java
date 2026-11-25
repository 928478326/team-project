package interface_adapter.calendar;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import use_case.view_calendar.ViewCalendarOutputBoundary;
import use_case.view_calendar.ViewCalendarOutputData;

/**
 * Presenter for the personal Calendar view.
 *
 * <p>Converts each UTC start time into the student's zone and records the local day of the month
 * and time, so the view can place events in the correct grid cell.
 *
 * <p>Calendars load in the background, so a result can arrive after the student who asked for it
 * has signed out. Results for anyone other than the currently signed-in student are dropped,
 * so the next student never sees someone else's events.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class ViewCalendarPresenter implements ViewCalendarOutputBoundary {

    /** Example: {@code Thu Dec 3, 14:00}. English names regardless of the computer's language. */
    private static final DateTimeFormatter EVENT_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM d, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final CalendarViewModel viewModel;
    private final SessionViewModel sessionViewModel;

    /**
     * Creates the presenter.
     *
     * @param viewModel calendar view model to update
     * @param sessionViewModel current sign-in state, used to drop results for a previous student
     */
    public ViewCalendarPresenter(CalendarViewModel viewModel, SessionViewModel sessionViewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.sessionViewModel =
                Objects.requireNonNull(sessionViewModel, "sessionViewModel must not be null");
    }

    @Override
    public void prepareSuccessView(ViewCalendarOutputData outputData) {
        if (isForSignedInStudent(outputData.getUserId())) {
            showMonth(outputData);
        }
    }

    @Override
    public void prepareFailView(String userId, String errorMessage) {
        if (isForSignedInStudent(userId)) {
            final CalendarState state = viewModel.getState();
            if (!state.getOwnerUserId().equals(userId)) {
                // Never show the previous student's events next to this student's error.
                state.setEvents(new ArrayList<>());
                state.setHolidays(new ArrayList<>());
                state.setOwnerUserId(userId);
            }
            state.setStatusMessage("");
            state.setErrorMessage(errorMessage);
            viewModel.firePropertyChanged();
        }
    }

    /**
     * Checks a result against the current session. A signed-out session accepts only
     * results that belong to nobody, such as "Sign in to view your calendar."
     *
     * @param userId the student the result belongs to
     * @return {@code true} if it may be shown now
     */
    private boolean isForSignedInStudent(String userId) {
        final SessionState session = sessionViewModel.getState();
        final String currentUserId;
        if (session != null && session.isLoggedIn()) {
            currentUserId = session.getUserId();
        }
        else {
            currentUserId = "";
        }
        return currentUserId.equals(userId);
    }

    private void showMonth(ViewCalendarOutputData outputData) {
        final ZoneId zone = ZoneId.of(outputData.getZoneId());
        final List<CalendarState.CalendarEventRow> eventRows = new ArrayList<>();
        for (ViewCalendarOutputData.CalendarEventItem item : outputData.getEvents()) {
            final ZonedDateTime local = item.getStartsAt().atZone(zone);
            eventRows.add(
                    new CalendarState.CalendarEventRow(
                            item.getEventId(),
                            local.format(EVENT_FORMATTER),
                            item.getCommunityName(),
                            item.getTitle(),
                            local.getDayOfMonth(),
                            local.format(TIME_FORMATTER)));
        }
        final List<CalendarState.HolidayRow> holidays = new ArrayList<>();
        for (ViewCalendarOutputData.HolidayItem holiday : outputData.getHolidays()) {
            holidays.add(new CalendarState.HolidayRow(holiday.getDate().getDayOfMonth(), holiday.getName()));
        }
        final CalendarState state = viewModel.getState();
        state.setYear(outputData.getYear());
        state.setMonth(outputData.getMonth());
        state.setZoneLabel(outputData.getZoneId());
        state.setEvents(eventRows);
        state.setHolidays(holidays);
        state.setErrorMessage("");
        state.setStatusMessage(outputData.getStatusMessage());
        state.setOwnerUserId(outputData.getUserId());
        viewModel.firePropertyChanged();
    }
}
