package use_case.create_event;

import java.time.Instant;

/**
 * Immutable output data for a newly created event.
 */
public final class CreateEventOutputData {

    private final String eventId;
    private final String communityId;
    private final String title;
    private final int capacity;
    private final Instant startsAt;

    public CreateEventOutputData(
            String eventId, String communityId, String title, int capacity, Instant startsAt) {
        this.eventId = eventId;
        this.communityId = communityId;
        this.title = title;
        this.capacity = capacity;
        this.startsAt = startsAt;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCommunityId() {
        return communityId;
    }

    public String getTitle() {
        return title;
    }

    public int getCapacity() {
        return capacity;
    }

    public Instant getStartsAt() {
        return startsAt;
    }
}
