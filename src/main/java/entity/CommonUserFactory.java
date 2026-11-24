package entity;

import java.util.Collections;

/**
 * Default {@link UserFactory} implementation that initializes new accounts with
 * {@link WeekendStyle#INDOOR_CHILL}, empty bio/contact info, and an empty interest set.
 */
public final class CommonUserFactory implements UserFactory {

    @Override
    public User create(String userId, String username, String passwordHash) {
        final String normalizedUsername = User.normalizeUsername(username);
        final PublicProfile initialProfile = new PublicProfile(
                normalizedUsername,
                "",
                "",
                WeekendStyle.INDOOR_CHILL,
                Collections.emptySet());
        return new User(userId, normalizedUsername, passwordHash, initialProfile);
    }
}
