package entity;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing a user's participation in an {@link Event}.
 * Uniquely identified by the composite key {@code (userId, eventId)}.
 */
public final class Participation {

    private final String userId;
    private final String eventId;
    private final Instant joinedAt;

    /**
     * Constructs an immutable {@code Participation}.
     *
     * @param userId participant user ID (non-blank)
     * @param eventId joined event ID (non-blank)
     * @param joinedAt UTC instant when the user first joined (non-null)
     */
    public Participation(String userId, String eventId, Instant joinedAt) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank.");
        }
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId must not be blank.");
        }
        this.userId = userId.trim();
        this.eventId = eventId.trim();
        this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt must not be null");
    }

    public String getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
