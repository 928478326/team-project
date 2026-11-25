package use_case.edit_profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import entity.LlmPersona;
import entity.PrivateProfile;
import entity.PublicProfile;
import entity.User;
import entity.WeekendStyle;

/**
 * Unit tests for {@link EditProfileInteractor} and its DTO/Gateway contracts using pure FAKE
 * collaborators (zero Swing, SQLite, or HTTP dependencies).
 */
class EditProfileInteractorTest {

    private static final int EXPECTED_FAILURES_THREE = 3;
    private static final int EXPECTED_FAILURES_FOUR = 4;
    private static final int LEGACY_NAME_LENGTH = 81;

    @Test
    void testLoadProfileSuccessHandlesLegacyLongNameAndProjectsSelfOnlyFields() {
        final FakeEditProfileDataAccess fakeDao = new FakeEditProfileDataAccess();
        final String legacyLongName = "h".repeat(LEGACY_NAME_LENGTH);
        final User henry = new User(
                "u-henry",
                "henry",
                "hash-123",
                new PublicProfile(
                        legacyLongName,
                        "Boardgames & study",
                        "discord: henry",
                        WeekendStyle.INDOOR_CHILL,
                        Set.of("study", "gaming")),
                new PrivateProfile("henryz07", false).markUoftVerified(),
                LlmPersona.of("Internal hidden LLM persona text"));
        fakeDao.seed(henry);

        final FakeEditProfilePresenter fakePresenter = new FakeEditProfilePresenter();
        final EditProfileInputBoundary interactor = new EditProfileInteractor(fakeDao, fakePresenter);

        interactor.execute(new EditProfileInputData(
                "u-henry",
                EditProfileInputData.Action.LOAD,
                "",
                "",
                "",
                "",
                Collections.emptySet()));

        assertEquals(1, fakePresenter.successCalls);
        assertEquals(0, fakePresenter.failCalls);
        assertEquals(0, fakeDao.updateCallCount);
        assertNotNull(fakePresenter.lastSuccess);
        assertEquals("u-henry", fakePresenter.lastSuccess.getUserId());
        assertEquals(legacyLongName, fakePresenter.lastSuccess.getDisplayName());
        assertEquals("Boardgames & study", fakePresenter.lastSuccess.getBio());
        assertEquals("discord: henry", fakePresenter.lastSuccess.getContactInfo());
        assertEquals("INDOOR_CHILL", fakePresenter.lastSuccess.getWeekendStyleCode());
        assertEquals(
                Set.of("study", "gaming"),
                fakePresenter.lastSuccess.getInterestedCommunityIds());
        assertEquals("henryz07", fakePresenter.lastSuccess.getUtorid());
        assertTrue(fakePresenter.lastSuccess.hasUtorid());
        assertFalse(fakePresenter.lastSuccess.isShareContactInfo());
        assertTrue(fakePresenter.lastSuccess.isUoftVerified());
    }

    @Test
    void testLoadProfileFailsWhenInputNullOrUserIdBlankOrMissing() {
        final FakeEditProfileDataAccess fakeDao = new FakeEditProfileDataAccess();
        final FakeEditProfilePresenter fakePresenter = new FakeEditProfilePresenter();
        final EditProfileInputBoundary interactor = new EditProfileInteractor(fakeDao, fakePresenter);

        interactor.execute(null);
        interactor.execute(new EditProfileInputData(
                "   ",
                EditProfileInputData.Action.LOAD,
                "",
                "",
                "",
                "",
                Collections.emptySet()));
        interactor.execute(new EditProfileInputData(
                "u-nonexistent",
                EditProfileInputData.Action.LOAD,
                "",
                "",
                "",
                "",
                Collections.emptySet()));

        assertEquals(0, fakePresenter.successCalls);
        assertEquals(EXPECTED_FAILURES_THREE, fakePresenter.failCalls);
        assertFalse(fakePresenter.lastError.isBlank());
    }

    @Test
    void testLoadReportsStorageFailure() {
        final FakeEditProfileDataAccess fakeDao = new FakeEditProfileDataAccess();
        fakeDao.failOnRead = true;
        final FakeEditProfilePresenter presenter = new FakeEditProfilePresenter();
        final EditProfileInteractor interactor = new EditProfileInteractor(fakeDao, presenter);

        interactor.execute(new EditProfileInputData(
                "u-henry", EditProfileInputData.Action.LOAD, "", "", "", "", Set.of()));

        assertEquals(1, presenter.failCalls);
        assertEquals("Could not load the profile. Please try again.", presenter.lastError);
        assertEquals(0, presenter.successCalls);
        assertEquals(0, fakeDao.updateCallCount);
    }

    @Test
    void testSaveReportsStorageFailureAndCanBeRetried() {
        final FakeEditProfileDataAccess fakeDao = new FakeEditProfileDataAccess();
        fakeDao.seed(new User("u-henry", "henry", "hash",
                new PublicProfile("Henry", "Original bio", "", WeekendStyle.INDOOR_CHILL, Set.of())));
        fakeDao.failOnUpdate = true;
        final FakeEditProfilePresenter presenter = new FakeEditProfilePresenter();
        final EditProfileInteractor interactor = new EditProfileInteractor(fakeDao, presenter);
        final EditProfileInputData edit = new EditProfileInputData(
                "u-henry", EditProfileInputData.Action.SAVE, "Henry", "New bio", "", "hiking", Set.of());

        interactor.execute(edit);

        assertEquals(1, presenter.failCalls);
        assertEquals("Could not save the profile. Please try again.", presenter.lastError);
        assertEquals(0, presenter.successCalls);
        assertEquals("Original bio", fakeDao.findUserById("u-henry").orElseThrow().getBio());

        fakeDao.failOnUpdate = false;
        interactor.execute(edit);

        assertEquals(1, presenter.failCalls);
        assertEquals(1, presenter.successCalls);
        assertEquals("New bio", fakeDao.findUserById("u-henry").orElseThrow().getBio());
    }

    @Test
    void testSavePublicOnlyPreservesExistingPrivateProfileAndSupportsFreeTextStyle() {
        final FakeEditProfileDataAccess fakeDao = new FakeEditProfileDataAccess();
        final User original = new User(
                "u-henry",
                "henry",
                "hash-123",
                new PublicProfile(
                        "Henry",
                        "Old bio",
                        "old-contact",
                        WeekendStyle.INDOOR_CHILL,
                        Set.of("study")),
                new PrivateProfile("henryz07", false),
                LlmPersona.of("Prior persona"));
        fakeDao.seed(original);

        final FakeEditProfilePresenter fakePresenter = new FakeEditProfilePresenter();
        final EditProfileInputBoundary interactor = new EditProfileInteractor(fakeDao, fakePresenter);

        final String customStyle = "Saturday badminton at Goldring & Sunday Robarts coding";
        interactor.execute(new EditProfileInputData(
                "u-henry",
                EditProfileInputData.Action.SAVE,
                "Henry Z",
                "Updated bio",
                "ig: @henry.uoft",
                customStyle,
                Set.of("Study", "FOOD")));

        assertEquals(1, fakePresenter.successCalls);
        assertEquals(1, fakeDao.updateCallCount);

        final User persisted = fakeDao.findUserById("u-henry").orElseThrow();
        assertEquals("Henry Z", persisted.getDisplayName());
        assertEquals(WeekendStyle.of(customStyle), persisted.getWeekendStyle());
        assertEquals(Set.of("study", "food"), persisted.getInterestedCommunityIds());
        assertEquals("henryz07", persisted.getPrivateProfile().getUtorid());
        assertFalse(persisted.getPrivateProfile().isShareContactInfo());
        assertEquals("Prior persona", persisted.getLlmPersona().getSummary());
        assertEquals(customStyle, fakePresenter.lastSuccess.getWeekendStyleCode());
        assertFalse(fakePresenter.lastSuccess.getStatusMessage().isBlank());
    }

    @Test
    void testSaveFullProfileEnrichesPersonaAndDoesNotAutoVerifySelfEnteredUtorid() {
        final FakeEditProfileDataAccess fakeDao = new FakeEditProfileDataAccess();
        fakeDao.seed(new User(
                "u-henry",
                "henry",
                "hash-123",
                new PublicProfile(
                        "Henry",
                        "Initial bio",
                        "discord: secret_handle",
                        WeekendStyle.INDOOR_CHILL,
                        Set.of("study"))));

        final FakeEditProfilePresenter fakePresenter = new FakeEditProfilePresenter();
        final List<LlmProfileEnrichmentGateway.SanitizedProfileSnapshot> captured =
                new ArrayList<>();
        final LlmProfileEnrichmentGateway fakeLlmGateway = snapshot -> {
            captured.add(snapshot);
            return LlmPersona.of(
                    "Synthesized: " + snapshot.getInterestedCommunityIds());
        };
        final EditProfileInputBoundary interactor =
                new EditProfileInteractor(fakeDao, fakePresenter, fakeLlmGateway);

        final EditProfileInputData input = new EditProfileInputData(
                "u-henry",
                EditProfileInputData.Action.SAVE,
                "henry",
                "Contact me at personal@example.invalid",
                "discord: private_handle",
                "Contact me at personal@example.invalid",
                Set.of("study", "food", "personal@example.invalid"))
                .withPrivateSettings("ZhouHaol07", false);
        interactor.execute(input);

        assertEquals(1, fakePresenter.successCalls);
        assertEquals(1, captured.size());
        assertEquals("", captured.get(0).getWeekendStyleText());
        assertEquals(Set.of("study", "food"), captured.get(0).getInterestedCommunityIds());
        assertEquals("Contact me at personal@example.invalid", fakePresenter.lastSuccess.getBio());

        final User persisted = fakeDao.findUserById("u-henry").orElseThrow();
        assertEquals("zhouhaol07", persisted.getPrivateProfile().getUtorid());
        assertTrue(persisted.hasUtorid());
        assertFalse(persisted.isUoftVerified());
        assertFalse(persisted.getPrivateProfile().isShareContactInfo());
        assertTrue(persisted.getLlmPersona().getSummary().startsWith("Synthesized:"));

        assertTrue(fakePresenter.lastSuccess.hasUtorid());
        assertFalse(fakePresenter.lastSuccess.isUoftVerified());
        assertFalse(fakePresenter.lastSuccess.isShareContactInfo());
    }

    @Test
    void unstructuredPreferencesStayLocalAndKeepThePreviousSummary() {
        final FakeEditProfileDataAccess storage = new FakeEditProfileDataAccess();
        final String contact = "local@example.invalid";
        final User student = new User("student", "student", "fixture-hash",
                new PublicProfile("student", "", contact, WeekendStyle.STUDY_FOCUS, Set.of()),
                new PrivateProfile("privateid", false), LlmPersona.of("Previous " + contact));
        storage.seed(student);
        final FakeEditProfilePresenter presenter = new FakeEditProfilePresenter();
        final EditProfileInteractor interactor = new EditProfileInteractor(storage, presenter, snapshot -> {
            throw new AssertionError("A save with only custom text should stay local");
        });
        interactor.execute(new EditProfileInputData("student", EditProfileInputData.Action.SAVE,
                "student", contact, contact, "Saturday " + contact, Set.of(contact)));
        assertEquals(1, presenter.successCalls);
        final User saved = storage.findUserById("student").orElseThrow();
        assertEquals(student.getLlmPersona(), saved.getLlmPersona());
        assertEquals(contact, saved.getBio());
        assertEquals("Saturday " + contact, saved.getWeekendStyle().getDisplayName());
    }

    @Test
    void testSaveDegradesGracefullyWhenLlmGatewayThrowsOrReturnsNull() {
        final FakeEditProfileDataAccess fakeDao = new FakeEditProfileDataAccess();
        final LlmPersona prior = LlmPersona.of("Existing persona");
        fakeDao.seed(new User(
                "u-henry",
                "henry",
                "hash-123",
                new PublicProfile("Henry", "", "", WeekendStyle.INDOOR_CHILL, Set.of("study")),
                PrivateProfile.defaultUnverified(),
                prior));

        final FakeEditProfilePresenter fakePresenter = new FakeEditProfilePresenter();
        final LlmProfileEnrichmentGateway throwingGateway = snapshot -> {
            throw new IllegalStateException("LLM API timeout");
        };
        final EditProfileInputBoundary interactorWithThrow =
                new EditProfileInteractor(fakeDao, fakePresenter, throwingGateway);
        interactorWithThrow.execute(new EditProfileInputData(
                "u-henry",
                EditProfileInputData.Action.SAVE,
                "Henry 1",
                "Bio",
                "",
                "STUDY_FOCUS",
                Set.of("study")));

        assertEquals(1, fakePresenter.successCalls);
        assertEquals(prior, fakeDao.findUserById("u-henry").orElseThrow().getLlmPersona());

        final EditProfileInputBoundary interactorWithNull =
                new EditProfileInteractor(fakeDao, fakePresenter, snapshot -> null);
        interactorWithNull.execute(new EditProfileInputData(
                "u-henry",
                EditProfileInputData.Action.SAVE,
                "Henry 2",
                "Bio",
                "",
                "STUDY_FOCUS",
                Set.of("study")));

        assertEquals(2, fakePresenter.successCalls);
        assertEquals(prior, fakeDao.findUserById("u-henry").orElseThrow().getLlmPersona());
    }

    @Test
    void testSaveValidationFailuresIncludingLengthBoundsDoNotMutateDao() {
        final FakeEditProfileDataAccess fakeDao = new FakeEditProfileDataAccess();
        fakeDao.seed(new User(
                "u-henry",
                "henry",
                "hash-123",
                new PublicProfile("Henry", "Bio", "contact", WeekendStyle.INDOOR_CHILL, Set.of("study"))));

        final FakeEditProfilePresenter fakePresenter = new FakeEditProfilePresenter();
        final EditProfileInputBoundary interactor = new EditProfileInteractor(fakeDao, fakePresenter);

        interactor.execute(new EditProfileInputData(
                "u-henry", EditProfileInputData.Action.SAVE, "   ", "Bio", "c", "INDOOR_CHILL", null));
        interactor.execute(new EditProfileInputData(
                "u-henry",
                EditProfileInputData.Action.SAVE,
                "a".repeat(PublicProfile.MAX_DISPLAY_NAME_LENGTH + 1),
                "Bio",
                "c",
                "INDOOR_CHILL",
                null));
        interactor.execute(new EditProfileInputData(
                "u-henry", EditProfileInputData.Action.SAVE, "Henry", "Bio", "c", "   ", null));
        interactor.execute(new EditProfileInputData(
                "u-henry", EditProfileInputData.Action.SAVE, "Henry", "Bio", "c", "INDOOR_CHILL", null)
                .withPrivateSettings("bad_utorid!", true));

        assertEquals(0, fakePresenter.successCalls);
        assertEquals(EXPECTED_FAILURES_FOUR, fakePresenter.failCalls);
        assertEquals(0, fakeDao.updateCallCount);
        assertNull(fakePresenter.lastSuccess);
        assertEquals("Henry", fakeDao.findUserById("u-henry").orElseThrow().getDisplayName());
    }

    /** Fake in-memory DAO implementing only {@link EditProfileDataAccessInterface}. */
    private static final class FakeEditProfileDataAccess implements EditProfileDataAccessInterface {

        private final Map<String, User> users = new HashMap<>();
        private int updateCallCount;
        private boolean failOnRead;
        private boolean failOnUpdate;

        void seed(User user) {
            users.put(user.getUserId(), user);
        }

        @Override
        public Optional<User> findUserById(String userId) {
            if (failOnRead) {
                throw new IllegalStateException("Could not read account.");
            }
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public boolean updateIfUnchanged(User expected, User updated) {
            final boolean matches = users.get(updated.getUserId()).hasSameProfileAs(expected);
            if (matches) {
                updateUserProfile(updated);
            }
            return matches;
        }

        @Override
        public void updateUserProfile(User updatedUser) {
            updateCallCount++;
            if (failOnUpdate) {
                throw new IllegalStateException("Could not update profile.");
            }
            users.put(updatedUser.getUserId(), updatedUser);
        }
    }

    /** Fake OutputBoundary recording presenter callbacks without any ViewModel or Swing UI. */
    private static final class FakeEditProfilePresenter implements EditProfileOutputBoundary {

        private EditProfileOutputData lastSuccess;
        private String lastError;
        private int successCalls;
        private int failCalls;

        @Override
        public void prepareSuccessView(EditProfileOutputData outputData) {
            successCalls++;
            lastSuccess = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            failCalls++;
            lastError = errorMessage;
        }
    }
}
