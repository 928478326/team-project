package interface_adapter.calendar;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import use_case.view_calendar.ViewCalendarOutputBoundary;
import use_case.view_calendar.ViewCalendarOutputData;

/**
 * Presenter for the personal Calendar view.
 *
 * <p>Converts each UTC start time into the student's zone and records the local day of the month
 * and time, so the view can place events in the correct grid cell.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class ViewCalendarPresenter implements ViewCalendarOutputBoundary {

    /** Example: {@code Thu Dec 3, 14:00}. English names regardless of the computer's language. */
    private static final DateTimeFormatter EVENT_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM d, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final CalendarViewModel viewModel;

    public ViewCalendarPresenter(CalendarViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
    }

    @Override
    public void prepareSuccessView(ViewCalendarOutputData outputData) {
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
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        final CalendarState state = viewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChanged();
    }
}
