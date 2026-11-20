package entity;

import java.util.Collections;

/**
 * Default {@link UserFactory} implementation that initializes new accounts with
 * {@link WeekendStyle#INDOOR_CHILL}, empty bio/contact info, and an empty interest set.
 */
public final class CommonUserFactory implements UserFactory {

    @Override
    public User create(String userId, String username, String passwordHash) {
        String normalizedUsername = User.normalizeUsername(username);
        return new User(
                userId,
                normalizedUsername,
                passwordHash,
                normalizedUsername,
                "",
                "",
                WeekendStyle.INDOOR_CHILL,
                Collections.emptySet());
    }
}
