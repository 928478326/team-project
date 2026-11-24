package data_access.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.SqliteDataAccessBundle;
import entity.CommonUserFactory;
import entity.User;
import entity.WeekendStyle;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteAccountTest {
    @Test
    void reopeningTheDatabasePreservesTheRegisteredIdentity(@TempDir Path directory) {
        String path = directory.resolve("data/campusbuddy.db").toString();
        SqliteDataAccessBundle first = new SqliteDataAccessBundle(path);
        User user = new CommonUserFactory().create("u-maple", "Maple", first.passwordHasher().hash("secret1"));
        assertTrue(first.signupDataAccess().saveUser(user));
        User profile = user.withProfile("Maple Leaf", "Studies at Robarts", "", WeekendStyle.STUDY_FOCUS,
                Set.of("food", "study"));
        first.editProfileDataAccess().updateUserProfile(profile);

        SqliteDataAccessBundle reopened = new SqliteDataAccessBundle(path);
        User saved = reopened.loginDataAccess().findByUsername("maple").orElseThrow();
        assertEquals(user.getUserId(), saved.getUserId());
        assertEquals(profile.getDisplayName(), saved.getDisplayName());
        assertEquals(profile.getInterestedCommunityIds(), saved.getInterestedCommunityIds());
        assertEquals(user.getPasswordHash(), saved.getPasswordHash());
        assertTrue(reopened.passwordHasher().matches("secret1", saved.getPasswordHash()));
        assertFalse(reopened.passwordHasher().matches("incorrect", saved.getPasswordHash()));
        assertTrue(reopened.loginDataAccess().findByUsername("missing").isEmpty());

        User duplicate = new CommonUserFactory().create("different-id", "MAPLE", reopened.passwordHasher().hash("secret1"));
        assertNotEquals(user.getPasswordHash(), duplicate.getPasswordHash());
        assertFalse(reopened.signupDataAccess().saveUser(duplicate));
        assertTrue(reopened.editProfileDataAccess().findUserById(duplicate.getUserId()).isEmpty());
    }
}
