package use_case.join_event;

/**
 * Immutable input data for joining a community event.
 */
public final class JoinEventInputData {

    private final String userId;
    private final String eventId;

    public JoinEventInputData(String userId, String eventId) {
        this.userId = userId == null ? "" : userId.trim();
        this.eventId = eventId == null ? "" : eventId.trim();
    }

    public String getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
    }
}
