package view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import entity.Community;
import entity.Event;
import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.calendar.CalendarViewModel;
import interface_adapter.calendar.ViewCalendarController;
import interface_adapter.calendar.ViewCalendarPresenter;
import use_case.view_calendar.ViewCalendarDataAccessInterface;
import use_case.view_calendar.ViewCalendarInteractor;

/**
 * Reproduces the review scenario: Alice's calendar load is slow, she signs out, Bob signs in and
 * opens the calendar. Bob's load must run, and Alice's late result must never reach his screen.
 */
class CalendarViewSessionTest {

    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");
    private static final String ALICE = "u-alice";
    private static final String BOB = "u-bob";
    private static final long TIMEOUT_MS = 10_000;

    private final CountDownLatch releaseAlice = new CountDownLatch(1);
    private final CountDownLatch aliceFinished = new CountDownLatch(1);
    private final SessionViewModel session = new SessionViewModel();
    private final CalendarViewModel calendarViewModel = new CalendarViewModel();

    @Test
    void lateResultForThePreviousStudentNeverReachesTheNextStudent() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Needs a display for Swing.");

        final CalendarView[] holder = new CalendarView[1];
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new CalendarView(calendarViewModel, session);
            holder[0].setViewCalendarController(new ViewCalendarController(new ViewCalendarInteractor(
                    new SlowForAliceDataAccess(), (year, country) -> List.of(), (events, zone, path) -> { },
                    new ViewCalendarPresenter(calendarViewModel, session), TORONTO)));
        });
        final CalendarView view = holder[0];
        final JTextArea agenda = find(view, JTextArea.class);
        final JButton nextMonth = findButton(view, "Next >");

        signIn(ALICE);
        SwingUtilities.invokeAndWait(view::refreshData);
        waitUntil(() -> !nextMonth.isEnabled());

        // Alice signs out while her load is still running; Bob signs in and opens the calendar.
        SwingUtilities.invokeAndWait(() -> {
            session.setState(SessionState.loggedOut());
            session.firePropertyChanged();
            session.setState(SessionState.loggedIn(BOB, "bob", "Bob"));
            session.firePropertyChanged();
            view.refreshData();
        });
        waitUntil(() -> agenda.getText().contains("Bob's review"));

        releaseAlice.countDown();
        assertTrue(aliceFinished.await(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        // Give Alice's worker time to reach the presenter and post any update before checking.
        Thread.sleep(300);
        flushEventQueue();

        assertTrue(agenda.getText().contains("Bob's review"), agenda.getText());
        assertFalse(agenda.getText().contains("Alice"), agenda.getText());
        assertTrue(nextMonth.isEnabled());
    }

    private void signIn(String userId) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            session.setState(SessionState.loggedIn(userId, userId, userId));
            session.firePropertyChanged();
        });
    }

    private static void waitUntil(BooleanSupplier condition) throws Exception {
        final long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        boolean met = false;
        while (!met && System.currentTimeMillis() < deadline) {
            flushEventQueue();
            met = condition.getAsBoolean();
            if (!met) {
                Thread.sleep(20);
            }
        }
        assertTrue(met, "condition not met within " + TIMEOUT_MS + " ms");
    }

    private static void flushEventQueue() throws Exception {
        // Twice: once for queued updates, once for anything those updates queued.
        SwingUtilities.invokeAndWait(() -> { });
        SwingUtilities.invokeAndWait(() -> { });
    }

    private static <T extends Component> T find(Container root, Class<T> type) {
        T found = null;
        for (Component child : root.getComponents()) {
            if (found == null && type.isInstance(child)) {
                found = type.cast(child);
            }
            else if (found == null && child instanceof Container) {
                found = find((Container) child, type);
            }
        }
        return found;
    }

    private static JButton findButton(Container root, String text) {
        JButton found = null;
        for (Component child : root.getComponents()) {
            if (found == null && child instanceof JButton && text.equals(((JButton) child).getText())) {
                found = (JButton) child;
            }
            else if (found == null && child instanceof Container) {
                found = findButton((Container) child, text);
            }
        }
        return found;
    }

    /** Alice's query blocks until the test releases it; Bob's returns at once. */
    private final class SlowForAliceDataAccess implements ViewCalendarDataAccessInterface {
        private final Instant midMonth =
                YearMonth.now(TORONTO).atDay(15).atTime(12, 0).atZone(TORONTO).toInstant();

        @Override
        public List<Event> findJoinedEventsBetween(String userId, Instant start, Instant end) {
            final List<Event> events;
            if (ALICE.equals(userId)) {
                try {
                    releaseAlice.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                events = List.of(new Event("e-alice", Community.STUDY_ID, "u-x", "Alice's private study session",
                        "", 4, midMonth));
                aliceFinished.countDown();
            }
            else {
                events = List.of(new Event("e-bob", Community.STUDY_ID, "u-x", "Bob's review", "", 4, midMonth));
            }
            return events;
        }

        @Override
        public List<Community> findAllCommunities() {
            return Community.defaultCommunities();
        }
    }
}
