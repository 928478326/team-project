package interface_adapter.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import use_case.view_calendar.ViewCalendarOutputData;
import use_case.view_calendar.ViewCalendarOutputData.CalendarEventItem;
import use_case.view_calendar.ViewCalendarOutputData.HolidayItem;

class ViewCalendarPresenterTest {

    private static final String ALICE = "u-alice";
    private static final String BOB = "u-bob";

    private final CalendarViewModel viewModel = new CalendarViewModel();
    private final SessionViewModel sessionViewModel = new SessionViewModel();
    private final ViewCalendarPresenter presenter = new ViewCalendarPresenter(viewModel, sessionViewModel);

    private void signIn(String userId) {
        sessionViewModel.setState(SessionState.loggedIn(userId, userId, userId));
    }

    private static ViewCalendarOutputData monthFor(String userId, String eventTitle) {
        return new ViewCalendarOutputData(userId, 2026, 11, "America/Toronto",
                List.of(new CalendarEventItem("e-1", "Food", eventTitle, Instant.parse("2026-12-01T03:00:00Z"))),
                List.of(new HolidayItem(LocalDate.of(2026, 11, 11), "Remembrance Day")),
                "");
    }

    @Test
    void eventsAreFiledUnderTheirTorontoDay() {
        signIn(ALICE);
        // 03:00 UTC on Dec 1 is still 22:00 on Nov 30 in Toronto.
        presenter.prepareSuccessView(monthFor(ALICE, "Late hotpot"));

        final CalendarState state = viewModel.getState();
        assertEquals(ALICE, state.getOwnerUserId());
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
        signIn(ALICE);
        presenter.prepareSuccessView(monthFor(ALICE, "Late hotpot"));

        presenter.prepareFailView(ALICE, "Could not export the calendar: disk full");

        assertEquals(11, viewModel.getState().getMonth());
        assertEquals(1, viewModel.getState().getEvents().size());
        assertEquals("Could not export the calendar: disk full", viewModel.getState().getErrorMessage());
        assertEquals("", viewModel.getState().getStatusMessage());
    }

    @Test
    void lateResultForThePreviousStudentIsDropped() {
        signIn(BOB);
        final List<Object> fired = new ArrayList<>();
        viewModel.addPropertyChangeListener(evt -> fired.add(evt.getNewValue()));

        // Alice's slow load finishes after she signed out and Bob signed in.
        presenter.prepareSuccessView(monthFor(ALICE, "Alice's private study session"));
        presenter.prepareFailView(ALICE, "Could not load your calendar. Please try again.");

        assertTrue(fired.isEmpty());
        assertTrue(viewModel.getState().getEvents().isEmpty());
        assertEquals("", viewModel.getState().getErrorMessage());

        presenter.prepareSuccessView(monthFor(BOB, "Bob's review"));
        assertEquals(BOB, viewModel.getState().getOwnerUserId());
        assertEquals("Bob's review", viewModel.getState().getEvents().get(0).getTitle());
    }

    @Test
    void resultArrivingAfterLogoutIsDropped() {
        presenter.prepareSuccessView(monthFor(ALICE, "Alice's private study session"));

        assertTrue(viewModel.getState().getEvents().isEmpty());
        assertEquals("", viewModel.getState().getOwnerUserId());
    }

    @Test
    void failureForNewStudentNeverShowsThePreviousStudentsEvents() {
        signIn(ALICE);
        presenter.prepareSuccessView(monthFor(ALICE, "Alice's private study session"));

        signIn(BOB);
        presenter.prepareFailView(BOB, "Could not load your calendar. Please try again.");

        final CalendarState state = viewModel.getState();
        assertEquals(BOB, state.getOwnerUserId());
        assertTrue(state.getEvents().isEmpty());
        assertTrue(state.getHolidays().isEmpty());
        assertEquals("Could not load your calendar. Please try again.", state.getErrorMessage());
    }

    @Test
    void signedOutRequestStillGetsItsSignInMessage() {
        presenter.prepareFailView("", "Sign in to view your calendar.");

        assertEquals("Sign in to view your calendar.", viewModel.getState().getErrorMessage());
    }
}
