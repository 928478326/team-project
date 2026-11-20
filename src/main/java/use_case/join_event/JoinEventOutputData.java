package use_case.join_event;

/**
 * Immutable output data for joining an event.
 */
public final class JoinEventOutputData {

    private final String eventId;
    private final boolean alreadyJoined;
    private final int joinedCount;
    private final int capacity;
    private final String message;

    public JoinEventOutputData(
            String eventId, boolean alreadyJoined, int joinedCount, int capacity, String message) {
        this.eventId = eventId;
        this.alreadyJoined = alreadyJoined;
        this.joinedCount = joinedCount;
        this.capacity = capacity;
        this.message = message;
    }

    public String getEventId() {
        return eventId;
    }

    public boolean isAlreadyJoined() {
        return alreadyJoined;
    }

    public int getJoinedCount() {
        return joinedCount;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getMessage() {
        return message;
    }
}
