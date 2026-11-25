package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.recommendation.EditProfileController;
import interface_adapter.recommendation.EditProfilePresenter;
import interface_adapter.recommendation.RecommendationViewModel;
import use_case.edit_profile.EditProfileInputData;
import use_case.edit_profile.EditProfileOutputData;

/** Drives the actual form controls and simulates presenter callbacks without opening a frame. */
class ProfileAndRecommendViewTest {
    private static final String ID = "UTORid";
    private static final String NAME = "Display Name";
    private static final String SAVE = "Save Profile";
    private static final String STYLE = "Weekend Style";
    private static final String HIDDEN = "Hidden on match cards";
    private static final int WAIT_SECONDS = 5;
    private final SessionViewModel session = new SessionViewModel();
    private final RecommendationViewModel model = new RecommendationViewModel();
    private final AtomicReference<EditProfileInputData> submitted = new AtomicReference<>();
    private final CountDownLatch failureQueued = new CountDownLatch(1);
    private final CountDownLatch started = new CountDownLatch(1);
    private final CountDownLatch release = new CountDownLatch(1);
    private EditProfilePresenter presenter;
    private ProfileAndRecommendView view;

    @BeforeEach
    void openProfile() throws Exception {
        onEdt(() -> {
            view = new ProfileAndRecommendView(model, session);
            presenter = new EditProfilePresenter(model, session, ProfileAndRecommendViewTest::dispatch);
            view.setEditProfileController(new EditProfileController(this::handle));
            signIn("Alex");
            return null;
        });
    }

    private static void dispatch(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        }
        else {
            SwingUtilities.invokeLater(action);
        }
    }

    private void signIn(String owner) {
        session.setState(SessionState.loggedIn(owner, owner, owner));
        session.firePropertyChanged();
        view.refreshData();
    }

    private void handle(EditProfileInputData input) {
        if (input.getAction() == EditProfileInputData.Action.LOAD) {
            final boolean first = "Alex".equals(input.getUserId());
            final EditProfileOutputData loaded = new EditProfileOutputData(input.getUserId(), input.getUserId(),
                    "Board games", "private-contact", "INDOOR_CHILL", Set.of("chill", "legacy"), "Profile loaded.");
            if (first) {
                presenter.prepareSuccessView(loaded.withPrivateStatus("alex123", true, false, true));
            }
            else {
                presenter.prepareSuccessView(loaded);
            }
        }
        else {
            assertFalse(SwingUtilities.isEventDispatchThread());
            submitted.set(input);
            started.countDown();
            try {
                assertTrue(release.await(WAIT_SECONDS, TimeUnit.SECONDS));
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            presenter.prepareFailView(input.getUserId(), "Invalid UTORid");
            failureQueued.countDown();
        }
    }

    @Test
    void showsTheSavedCardAndSeparatesTheThreeIdentityStates() throws Exception {
        onEdt(() -> {
            assertEquals("Alex", text(NAME).getText());
            assertEquals("alex123", text(ID).getText());
            assertEquals("Indoor Chill", combo().getEditor().getItem());
            assertEquals("UofT verified", label("UofT verified").getText());
            assertTrue(button("chill").isSelected());
            assertEquals(HIDDEN, find(JTextArea.class, area -> HIDDEN.equals(area.getText())).getText());
            return null;
        });
        onEdt(() -> {
            presenter.prepareSuccessView(new EditProfileOutputData("Alex", "Alex", "", "", "Free text",
                    Set.of(), "Profile loaded.").withPrivateStatus("alex123", true, false, false));
            assertEquals("Recorded - pending verification", label("Recorded - pending verification").getText());
            signIn("Rowan");
            assertEquals("No UTORid recorded", label("No UTORid recorded").getText());
            assertEquals("", text(ID).getText());
            return null;
        });
    }

    @Test
    void workerCapturesUncommittedComboTextAndKeepsInvalidEditsForCorrection() throws Exception {
        onEdt(() -> {
            combo().getEditor().setItem("Walks after class");
            text(ID).setText("wrong!");
            button("food").setSelected(true);
            button(SAVE).doClick();
            assertFalse(button(SAVE).isEnabled());
            return null;
        });
        assertTrue(started.await(WAIT_SECONDS, TimeUnit.SECONDS));
        release.countDown();
        awaitEnabled();
        onEdt(() -> {
            assertEquals("Invalid UTORid", label("Invalid UTORid").getText());
            assertEquals("wrong!", text(ID).getText());
            assertEquals("Walks after class", submitted.get().getWeekendStyleCode());
            assertEquals(Set.of("chill", "food", "legacy"), submitted.get().getInterestedCommunityIds());
            assertFalse(submitted.get().isShareContactInfo());
            return null;
        });
    }

    @Test
    void aConflictingSaveKeepsTheDraftUntilTheStudentExplicitlyReloads() throws Exception {
        onEdt(() -> {
            text(NAME).setText("My unsaved edit");
            presenter.prepareFailView("Alex", "Your profile changed during this save. Reload it and try again.");
            assertEquals("My unsaved edit", text(NAME).getText());
            assertTrue(button("Discard edits and reload").isVisible());
            button("Discard edits and reload").doClick();
            assertEquals("Alex", text(NAME).getText());
            assertEquals("Profile loaded.", label("Profile loaded.").getText());
            return null;
        });
    }

    @Test
    void logoutDuringSaveDiscardsLateFailureForThePreviousOwner() throws Exception {
        onEdt(() -> {
            button(SAVE).doClick();
            return null;
        });
        assertTrue(started.await(WAIT_SECONDS, TimeUnit.SECONDS));
        onEdt(() -> {
            session.setState(SessionState.loggedOut());
            session.firePropertyChanged();
            return null;
        });
        onEdt(() -> {
            assertEquals("", text(ID).getText());
            assertEquals("", text(NAME).getText());
            assertEquals("", combo().getEditor().getItem());
            assertFalse(button("chill").isSelected());
            assertFalse(button(SAVE).isEnabled());
            signIn("Rowan");
            return null;
        });
        release.countDown();
        // Drain the old presenter's failure callback before inspecting the second account.
        assertTrue(failureQueued.await(WAIT_SECONDS, TimeUnit.SECONDS));
        onEdt(() -> {
            assertEquals("Rowan", text(NAME).getText());
            assertEquals("", text(ID).getText());
            assertEquals("Profile loaded.", label("Profile loaded.").getText());
            assertTrue(button(SAVE).isEnabled());
            return null;
        });
    }

    @Test
    void aFailedLoadOffersRetryBeforeEnablingTheForm() throws Exception {
        onEdt(() -> {
            view.setEditProfileController(new EditProfileController(input -> {
                presenter.prepareFailView(input.getUserId(), "Could not load profile.");
            }));
            signIn("Rowan");
            assertFalse(button(SAVE).isEnabled());
            assertTrue(button("Retry load").isVisible());
            return null;
        });
        onEdt(() -> {
            view.setEditProfileController(new EditProfileController(this::handle));
            button("Retry load").doClick();
            assertTrue(button(SAVE).isEnabled());
            assertEquals("Rowan", text(NAME).getText());
            return null;
        });
    }

    private void awaitEnabled() throws Exception {
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(WAIT_SECONDS);
        while (!onEdt(() -> button(SAVE).isEnabled())) {
            assertTrue(System.nanoTime() < deadline, "Save did not finish");
            TimeUnit.MILLISECONDS.sleep(10);
        }
    }

    private JLabel label(String value) {
        return find(JLabel.class, item -> value.equals(item.getText()));
    }

    private JTextField text(String caption) {
        return (JTextField) label(caption).getLabelFor();
    }

    private JComboBox<?> combo() {
        return (JComboBox<?>) label(STYLE).getLabelFor();
    }

    private AbstractButton button(String caption) {
        return find(AbstractButton.class, item -> caption.equals(item.getText()));
    }

    private <T extends Component> T find(Class<T> type, Predicate<T> match) {
        final Deque<Component> todo = new ArrayDeque<>();
        todo.add(view);
        T found = null;
        while (found == null && !todo.isEmpty()) {
            final Component next = todo.remove();
            if (type.isInstance(next) && match.test(type.cast(next))) {
                found = type.cast(next);
            }
            else if (next instanceof Container) {
                todo.addAll(Arrays.asList(((Container) next).getComponents()));
            }
        }
        if (found == null) {
            throw new AssertionError("Missing " + type.getSimpleName());
        }
        return found;
    }

    private static <T> T onEdt(Callable<T> call) throws Exception {
        final FutureTask<T> task = new FutureTask<>(call);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
