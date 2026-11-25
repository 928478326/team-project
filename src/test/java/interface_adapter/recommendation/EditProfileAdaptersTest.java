package interface_adapter.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import use_case.edit_profile.EditProfileInputData;
import use_case.edit_profile.EditProfileOutputData;

/** Covers profile form inputs and owner-only presenter updates. */
class EditProfileAdaptersTest {
    @Test
    void legacyAndExtendedFormsHaveDifferentPrivateUpdateSemantics() {
        final AtomicReference<EditProfileInputData> input = new AtomicReference<>();
        final EditProfileController controller = new EditProfileController(input::set);
        controller.saveProfile("a", "Name", "Bio", "contact", "weekend walks", Set.of("study"));
        assertFalse(input.get().isPrivateProfileIncluded());
        controller.saveProfile("a", "Name", "Bio", "contact", "weekend walks", Set.of("study"),
                new PrivateSettingsInput("demo123", false));
        assertTrue(input.get().isPrivateProfileIncluded());
        assertEquals("demo123", input.get().getUtorid());
        assertFalse(input.get().isShareContactInfo());
        assertEquals("weekend walks", input.get().getWeekendStyleCode());
        controller.loadProfile("a");
        assertEquals(EditProfileInputData.Action.LOAD, input.get().getAction());
    }

    @Test
    void nameChangeKeepsSaveConfirmationAfterTheShellReloadsTheActiveTab() {
        final SessionViewModel session = new SessionViewModel();
        session.setState(SessionState.loggedIn("a", "alice", "Alice"));
        final RecommendationViewModel model = new RecommendationViewModel();
        final EditProfilePresenter presenter = new EditProfilePresenter(model, session);
        final EditProfileOutputData loaded = new EditProfileOutputData("a", "New name", "", "",
                "walks", Set.of(), "Profile loaded.");
        session.addPropertyChangeListener(event -> presenter.prepareSuccessView(loaded));
        presenter.prepareSuccessView(new EditProfileOutputData("a", "New name", "", "",
                "walks", Set.of(), "Profile saved."));
        assertEquals("New name", session.getState().getDisplayName());
        assertEquals("Profile saved.", model.getState().getStatusMessage());
    }

    @Test
    void logoutClearsPrivateStateAndQueuedResultsCannotOverwriteAnotherOwner() {
        final SessionViewModel session = new SessionViewModel();
        session.setState(SessionState.loggedIn("a", "alice", "Alice"));
        final RecommendationViewModel model = new RecommendationViewModel();
        final Queue<Runnable> ui = new ArrayDeque<>();
        final EditProfilePresenter presenter = new EditProfilePresenter(model, session, ui::add);
        final EditProfileOutputData profile = new EditProfileOutputData("a", "Renamed", "Bio", "handle",
                "hiking", Set.of("sports"), "Profile saved.").withPrivateStatus("demo123", true, false, true);
        presenter.prepareSuccessView(profile);
        drain(ui);
        assertEquals("Renamed", session.getState().getDisplayName());
        assertEquals("demo123", model.getState().getUtorid());
        assertTrue(model.getState().hasUtorid());
        assertTrue(model.getState().isUoftVerified());
        assertFalse(model.getState().isShareContactInfo());
        presenter.prepareFailView("a", "Could not save.");
        drain(ui);
        assertEquals("Could not save.", model.getState().getErrorMessage());
        presenter.prepareSuccessView(profile);
        presenter.prepareFailView("a", "Late failure.");
        session.setState(SessionState.loggedOut());
        session.firePropertyChanged();
        session.setState(SessionState.loggedIn("b", "bob", "Bob"));
        session.firePropertyChanged();
        drain(ui);
        assertEquals("", model.getState().getUtorid());
        assertFalse(model.getState().hasUtorid());
        assertFalse(model.getState().isUoftVerified());
        assertEquals("", model.getState().getErrorMessage());
        assertEquals("Bob", session.getState().getDisplayName());
    }

    private static void drain(Queue<Runnable> actions) {
        while (!actions.isEmpty()) {
            actions.remove().run();
        }
    }
}
