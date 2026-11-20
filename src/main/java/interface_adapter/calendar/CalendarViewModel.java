package interface_adapter.calendar;

import interface_adapter.ViewModel;

/**
 * ViewModel for the personal Calendar view.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CalendarViewModel extends ViewModel<CalendarState> {

    public static final String VIEW_NAME = "calendar";

    public CalendarViewModel() {
        super(VIEW_NAME);
        setState(new CalendarState());
    }
}
