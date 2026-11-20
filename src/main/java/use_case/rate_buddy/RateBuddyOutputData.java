package use_case.rate_buddy;

/**
 * Immutable output data for a saved buddy rating.
 */
public final class RateBuddyOutputData {

    private final String rateeId;
    private final String eventId;
    private final int score;
    private final boolean wouldMeetAgain;
    private final String message;

    public RateBuddyOutputData(
            String rateeId, String eventId, int score, boolean wouldMeetAgain, String message) {
        this.rateeId = rateeId;
        this.eventId = eventId;
        this.score = score;
        this.wouldMeetAgain = wouldMeetAgain;
        this.message = message;
    }

    public String getRateeId() {
        return rateeId;
    }

    public String getEventId() {
        return eventId;
    }

    public int getScore() {
        return score;
    }

    public boolean isWouldMeetAgain() {
        return wouldMeetAgain;
    }

    public String getMessage() {
        return message;
    }
}
