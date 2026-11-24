package interface_adapter.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import use_case.view_calendar.ViewCalendarOutputData;
import use_case.view_calendar.ViewCalendarOutputData.CalendarEventItem;
import use_case.view_calendar.ViewCalendarOutputData.HolidayItem;

class ViewCalendarPresenterTest {

    private final CalendarViewModel viewModel = new CalendarViewModel();
    private final ViewCalendarPresenter presenter = new ViewCalendarPresenter(viewModel);

    @Test
    void eventsAreFiledUnderTheirTorontoDay() {
        // 03:00 UTC on Dec 1 is still 22:00 on Nov 30 in Toronto.
        presenter.prepareSuccessView(new ViewCalendarOutputData(2026, 11, "America/Toronto",
                List.of(new CalendarEventItem("e-1", "Food", "Late hotpot", Instant.parse("2026-12-01T03:00:00Z"))),
                List.of(new HolidayItem(LocalDate.of(2026, 11, 11), "Remembrance Day")),
                ""));

        final CalendarState state = viewModel.getState();
        assertEquals(2026, state.getYear());
        assertEquals(11, state.getMonth());
        final CalendarState.CalendarEventRow row = state.getEvents().get(0);
        assertEquals(30, row.getDayOfMonth());
        assertEquals("22:00", row.getTimeText());
        assertEquals("Mon Nov 30, 22:00", row.getFormattedDateTime());
        assertEquals(11, state.getHolidays().get(0).getDayOfMonth());
        assertEquals("Remembrance Day", state.getHolidays().get(0).getName());
        assertEquals("", state.getErrorMessage());
    }

    @Test
    void failureKeepsTheMonthAndShowsTheError() {
        final int month = viewModel.getState().getMonth();

        presenter.prepareFailView("Could not export the calendar: disk full");

        assertEquals(month, viewModel.getState().getMonth());
        assertEquals("Could not export the calendar: disk full", viewModel.getState().getErrorMessage());
        assertEquals("", viewModel.getState().getStatusMessage());
    }
}
