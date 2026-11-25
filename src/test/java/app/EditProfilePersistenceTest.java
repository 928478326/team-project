package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import data_access.gateway.RuleBasedProfileEnrichmentGateway;
import entity.CommonUserFactory;
import entity.PrivateProfile;
import entity.User;
import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.recommendation.EditProfileController;
import interface_adapter.recommendation.EditProfilePresenter;
import interface_adapter.recommendation.PrivateSettingsInput;
import interface_adapter.recommendation.RecommendationState;
import interface_adapter.recommendation.RecommendationViewModel;
import use_case.edit_profile.EditProfileInteractor;

/** Exercises the offline controller-to-storage flow and self-only readback. */
class EditProfilePersistenceTest {
    private static final String OWNER = "u-rowan";
    private static final String STYLE = "Walks by the lake";
    private final SessionViewModel session = new SessionViewModel();
    private final RecommendationViewModel profile = new RecommendationViewModel();

    @Test
    void sqliteRestoresPrivateSettingsAndGeneratedPersona(@TempDir Path folder) {
        final String file = folder.resolve("campusbuddy.db").toString();
        final DataAccessBundle first = new SqliteDataAccessBundle(file);
        save(first);
        final DataAccessBundle reopened = new SqliteDataAccessBundle(file);
        checkSavedProfile(reopened);
    }

    @Test
    void inMemoryModeSupportsTheSameSaveAndLoad() {
        final DataAccessBundle memory = new InMemoryDataAccessBundle();
        save(memory);
        checkSavedProfile(memory);
    }

    private void save(DataAccessBundle data) {
        final User user = new CommonUserFactory().create(OWNER, "rowan", "test-hash");
        assertTrue(data.signupDataAccess().saveUser(user));
        session.setState(SessionState.loggedIn(OWNER, "rowan", "rowan"));
        controller(data).saveProfile(OWNER, "Rowan", "Studies outdoors", "owner-only", STYLE, Set.of("study"),
                new PrivateSettingsInput("demo123", false));
        assertEquals("Profile saved.", profile.getState().getStatusMessage());
        assertEquals("Rowan", session.getState().getDisplayName());
    }

    private void checkSavedProfile(DataAccessBundle data) {
        profile.setState(new RecommendationState());
        controller(data).loadProfile(OWNER);
        final RecommendationState state = profile.getState();
        assertEquals("Profile loaded.", state.getStatusMessage());
        assertEquals(STYLE, state.getWeekendStyleCode());
        assertEquals("owner-only", state.getContactInfo());
        assertEquals("demo123", state.getUtorid());
        assertTrue(state.hasUtorid());
        assertFalse(state.isShareContactInfo());
        assertFalse(state.isUoftVerified());
        final User loaded = data.loginDataAccess().findByUsername("rowan").orElseThrow();
        assertEquals(PrivateProfile.HIDDEN_CONTACT_PLACEHOLDER, loaded.getPublicContactInfo());
        assertEquals("Weekend preference: Walks by the lake. Bio: Studies outdoors. Communities: study",
                loaded.getLlmPersona().getSummary());
    }

    private EditProfileController controller(DataAccessBundle data) {
        final EditProfilePresenter presenter = new EditProfilePresenter(profile, session);
        return new EditProfileController(new EditProfileInteractor(
                data.editProfileDataAccess(), presenter, new RuleBasedProfileEnrichmentGateway()));
    }
}
