package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import entity.CommonUserFactory;
import entity.LlmPersona;
import entity.User;
import use_case.edit_profile.EditProfileInputData;
import use_case.edit_profile.EditProfileInteractor;
import use_case.edit_profile.EditProfileOutputBoundary;
import use_case.edit_profile.EditProfileOutputData;

class ProfileConcurrentSaveTest {
    private static final String ID = "fixture-student";

    @Test
    void sqliteChecksForNewerEditsAcrossConnections(@TempDir Path directory) {
        final String path = directory.resolve("profiles.db").toString();
        checkOverlappingSaves(new SqliteDataAccessBundle(path), new SqliteDataAccessBundle(path));
    }

    @Test
    void inMemoryChecksForNewerEdits() {
        final DataAccessBundle data = new InMemoryDataAccessBundle();
        checkOverlappingSaves(data, data);
    }

    private void checkOverlappingSaves(DataAccessBundle first, DataAccessBundle second) {
        final User student = new CommonUserFactory().create(ID, "fixture", "fixture-hash");
        assertTrue(first.signupDataAccess().saveUser(student));
        final EditProfileInputData edit = new EditProfileInputData(ID, EditProfileInputData.Action.SAVE,
                "fixture", "Public draft", "Local contact", "STUDY_FOCUS", Set.of("study"));
        final RecordingPresenter earlier = new RecordingPresenter();
        final RecordingPresenter later = new RecordingPresenter();
        final EditProfileInteractor newer = new EditProfileInteractor(second.editProfileDataAccess(), later);
        final EditProfileInteractor older = new EditProfileInteractor(first.editProfileDataAccess(), earlier,
                preferences -> {
                    newer.execute(edit.withPrivateSettings("", false));
                    return LlmPersona.of("Earlier summary");
                });
        older.execute(edit);
        assertNotNull(later.success);
        assertNull(earlier.success);
        assertTrue(earlier.error.contains("changed"));
        final User saved = second.editProfileDataAccess().findUserById(ID).orElseThrow();
        assertFalse(saved.getPrivateProfile().isShareContactInfo());
        assertEquals("", saved.getLlmPersona().getSummary());
    }

    private static final class RecordingPresenter implements EditProfileOutputBoundary {
        private EditProfileOutputData success;
        private String error;

        @Override
        public void prepareSuccessView(EditProfileOutputData output) {
            success = output;
        }

        @Override
        public void prepareFailView(String message) {
            error = message;
        }
    }
}
