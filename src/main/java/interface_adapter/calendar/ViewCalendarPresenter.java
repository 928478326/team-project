package interface_adapter.calendar;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import use_case.view_calendar.ViewCalendarOutputBoundary;
import use_case.view_calendar.ViewCalendarOutputData;

/**
 * Presenter for the personal Calendar view.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class ViewCalendarPresenter implements ViewCalendarOutputBoundary {

    private static final DateTimeFormatter EVENT_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd HH:mm");
    private static final DateTimeFormatter HOLIDAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd");

    private final CalendarViewModel viewModel;

    public ViewCalendarPresenter(CalendarViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
    }

    @Override
    public void prepareSuccessView(ViewCalendarOutputData outputData) {
        // TODO [Track B]: Refine formatting as needed when building CalendarView.
        ZoneId zone = ZoneId.of(outputData.getZoneId());
        List<CalendarState.CalendarEventRow> eventRows = new ArrayList<>();
        for (ViewCalendarOutputData.CalendarEventItem item : outputData.getEvents()) {
            String formatted = item.getStartsAt().atZone(zone).format(EVENT_FORMATTER);
            eventRows.add(
                    new CalendarState.CalendarEventRow(
                            item.getEventId(), formatted, item.getCommunityName(), item.getTitle()));
        }
        List<String> holidayLabels = new ArrayList<>();
        for (ViewCalendarOutputData.HolidayItem holiday : outputData.getHolidays()) {
            holidayLabels.add(
                    holiday.getDate().format(HOLIDAY_FORMATTER) + " " + holiday.getName());
        }
        CalendarState state = viewModel.getState();
        state.setYear(outputData.getYear());
        state.setMonth(outputData.getMonth());
        state.setZoneLabel(outputData.getZoneId());
        state.setEvents(eventRows);
        state.setHolidayLabels(holidayLabels);
        state.setErrorMessage("");
        state.setStatusMessage(outputData.getStatusMessage());
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        CalendarState state = viewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChanged();
    }
}
