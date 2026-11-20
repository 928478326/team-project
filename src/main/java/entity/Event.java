package entity;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Domain entity representing a community event invitation with a participant capacity limit.
 */
public final class Event {

    /** Minimum allowed participant capacity for any event. */
    public static final int MIN_CAPACITY = 1;
    /** Maximum allowed participant capacity for MVP events. */
    public static final int MAX_CAPACITY = 100;

    private final String eventId;
    private final String communityId;
    private final String creatorId;
    private final String title;
    private final String description;
    private final int capacity;
    private final Instant startsAt;

    /**
     * Constructs an immutable {@code Event} and enforces core invariants.
     *
     * @param eventId unique event ID (non-blank)
     * @param communityId community ID this event belongs to (non-blank)
     * @param creatorId user ID of the creator (non-blank)
     * @param title event title (non-blank)
     * @param description optional event description
     * @param capacity maximum number of participants ({@value #MIN_CAPACITY}..{@value #MAX_CAPACITY})
     * @param startsAt UTC instant when the event begins (non-null)
     */
    public Event(
            String eventId,
            String communityId,
            String creatorId,
            String title,
            String description,
            int capacity,
            Instant startsAt) {
        this.eventId = requireNonBlank(eventId, "eventId");
        this.communityId = requireNonBlank(communityId, "communityId").toLowerCase(Locale.ROOT);
        this.creatorId = requireNonBlank(creatorId, "creatorId");
        this.title = requireNonBlank(title, "title");
        this.description = description == null ? "" : description.trim();
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException(
                    "Event capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY + ".");
        }
        this.capacity = capacity;
        this.startsAt = Objects.requireNonNull(startsAt, "startsAt must not be null");
    }

    /**
     * Checks whether the event is still open for joining at {@code now}.
     * Joining closes strictly at the event start instant.
     *
     * @param now current instant (non-null)
     * @return {@code true} if {@code now} is strictly before {@code startsAt}
     */
    public boolean isOpenAt(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return now.isBefore(startsAt);
    }

    /**
     * Checks whether another participant can still join given {@code currentJoinedCount}.
     *
     * @param currentJoinedCount number of participants currently enrolled
     * @return {@code true} if {@code currentJoinedCount < capacity}
     */
    public boolean hasRemainingSeat(int currentJoinedCount) {
        return currentJoinedCount < capacity;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCommunityId() {
        return communityId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value.trim();
    }
}
