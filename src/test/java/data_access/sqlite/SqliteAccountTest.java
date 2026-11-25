package data_access.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import app.SqliteDataAccessBundle;
import entity.CommonUserFactory;
import entity.LlmPersona;
import entity.PrivateProfile;
import entity.PublicProfile;
import entity.User;
import entity.WeekendStyle;

class SqliteAccountTest {

    private static final int TIMEOUT_SECONDS = 10;
    private static final int LEGACY_USERNAME_LENGTH = 81;
    private static final int LEGACY_BIO_LENGTH = 520;

    @Test
    void reopeningTheDatabasePreservesTheRegisteredIdentity(@TempDir Path directory) {
        final String path = directory.resolve("data/campusbuddy.db").toString();
        final SqliteDataAccessBundle first = new SqliteDataAccessBundle(path);
        final User user = new CommonUserFactory().create(
                "u-maple", "Maple", first.passwordHasher().hash("secret1"));
        assertTrue(first.signupDataAccess().saveUser(user));
        final User profile = user.withProfile(
                "Maple Leaf",
                "Studies at Robarts",
                "",
                WeekendStyle.STUDY_FOCUS,
                Set.of("food", "study"));
        first.editProfileDataAccess().updateUserProfile(profile);

        final SqliteDataAccessBundle reopened = new SqliteDataAccessBundle(path);
        final User saved = reopened.loginDataAccess().findByUsername("maple").orElseThrow();
        assertEquals(user.getUserId(), saved.getUserId());
        assertEquals(profile.getDisplayName(), saved.getDisplayName());
        assertEquals(profile.getInterestedCommunityIds(), saved.getInterestedCommunityIds());
        assertEquals(user.getPasswordHash(), saved.getPasswordHash());
        assertTrue(reopened.passwordHasher().matches("secret1", saved.getPasswordHash()));
        assertFalse(reopened.passwordHasher().matches("incorrect", saved.getPasswordHash()));
        assertTrue(reopened.loginDataAccess().findByUsername("missing").isEmpty());

        final User duplicate = new CommonUserFactory().create(
                "different-id", "MAPLE", reopened.passwordHasher().hash("secret1"));
        assertNotEquals(user.getPasswordHash(), duplicate.getPasswordHash());
        assertFalse(reopened.signupDataAccess().saveUser(duplicate));
        assertTrue(reopened.editProfileDataAccess().findUserById(duplicate.getUserId()).isEmpty());
    }

    @Test
    void reopeningDatabasePreservesHiddenContactPreferenceAndLlmPersona(@TempDir Path directory) {
        final String path = directory.resolve("data/campusbuddy.db").toString();
        final SqliteDataAccessBundle first = new SqliteDataAccessBundle(path);
        final PublicProfile initialPublic = new PublicProfile(
                "Henry",
                "Initial bio",
                "discord: secret_handle",
                WeekendStyle.of("Saturday badminton & Sunday library coding"),
                Set.of("study", "sports"));
        final PrivateProfile hiddenContact = new PrivateProfile("zhouhaol", false);
        final LlmPersona persona =
                LlmPersona.of("Prefers small evening study sessions; punctual.");
        final User initialUser = new User(
                "u-henry",
                "henry",
                first.passwordHasher().hash("secret1"),
                initialPublic,
                hiddenContact,
                persona);
        assertTrue(first.signupDataAccess().saveUser(initialUser));

        final User updated = initialUser
                .withProfile(
                        "Henry Z",
                        "Updated bio",
                        "discord: updated_secret",
                        WeekendStyle.of("Weekend cafe coding & boardgames"),
                        Set.of("study", "gaming"))
                .withPrivateProfile(hiddenContact.markUoftVerified());
        first.editProfileDataAccess().updateUserProfile(updated);

        final SqliteDataAccessBundle reopened = new SqliteDataAccessBundle(path);
        final User reloaded = reopened.loginDataAccess().findByUsername("henry").orElseThrow();
        assertFalse(reloaded.getPrivateProfile().isShareContactInfo());
        assertEquals(
                PrivateProfile.HIDDEN_CONTACT_PLACEHOLDER,
                reloaded.getPublicContactInfo());
        assertEquals("discord: updated_secret", reloaded.getContactInfo());
        assertTrue(reloaded.hasUtorid());
        assertTrue(reloaded.isUoftVerified());
        assertEquals("zhouhaol", reloaded.getPrivateProfile().getUtorid());
        assertEquals(persona, reloaded.getLlmPersona());
        assertEquals(
                WeekendStyle.of("Weekend cafe coding & boardgames"),
                reloaded.getWeekendStyle());
    }

    @Test
    void migratesLegacyEightColumnSchemaAndLoadsLongUsernameAccounts(@TempDir Path directory)
            throws Exception {
        final Path dbFile = directory.resolve("legacy/campusbuddy.db");
        Files.createDirectories(dbFile.getParent());
        final String longUsername = "u".repeat(LEGACY_USERNAME_LENGTH);
        final String longBio = "b".repeat(LEGACY_BIO_LENGTH);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
                Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE users ("
                    + "user_id TEXT PRIMARY KEY NOT NULL, username TEXT UNIQUE NOT NULL, "
                    + "password_hash TEXT NOT NULL, display_name TEXT NOT NULL, bio TEXT NOT NULL, "
                    + "contact_info TEXT NOT NULL, weekend_style TEXT NOT NULL, "
                    + "interested_communities TEXT NOT NULL)");
            stmt.execute("INSERT INTO users VALUES ('u-legacy', '"
                    + longUsername
                    + "', 'hash-legacy', '"
                    + longUsername
                    + "', '"
                    + longBio
                    + "', 'discord: legacy', 'INDOOR_CHILL', '[\"study\"]')");
        }

        final CyclicBarrier ready = new CyclicBarrier(2);
        final ExecutorService pool = Executors.newFixedThreadPool(2);
        final Callable<SqliteDataAccessBundle> open = () -> {
            ready.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return new SqliteDataAccessBundle(dbFile.toString());
        };
        final SqliteDataAccessBundle migrated;
        try {
            final Future<SqliteDataAccessBundle> first = pool.submit(open);
            final Future<SqliteDataAccessBundle> second = pool.submit(open);
            migrated = first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertTrue(second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .loginDataAccess().findByUsername(longUsername).isPresent());
        }
        finally {
            pool.shutdownNow();
        }
        final User loaded =
                migrated.loginDataAccess().findByUsername(longUsername).orElseThrow();
        assertEquals(longUsername, loaded.getDisplayName());
        assertEquals(longBio, loaded.getBio());
        assertTrue(loaded.getPrivateProfile().isShareContactInfo());
        assertEquals("discord: legacy", loaded.getPublicContactInfo());
        assertFalse(loaded.hasUtorid());
        assertFalse(loaded.isUoftVerified());
        assertTrue(loaded.getLlmPersona().isEmpty());
    }
}
