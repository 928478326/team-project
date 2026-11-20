package use_case.rate_buddy;

/**
 * Immutable input data for submitting or updating a buddy rating.
 */
public final class RateBuddyInputData {

    private final String raterId;
    private final String rateeId;
    private final String eventId;
    private final int score;
    private final boolean wouldMeetAgain;
    private final String comment;

    public RateBuddyInputData(
            String raterId,
            String rateeId,
            String eventId,
            int score,
            boolean wouldMeetAgain,
            String comment) {
        this.raterId = raterId == null ? "" : raterId.trim();
        this.rateeId = rateeId == null ? "" : rateeId.trim();
        this.eventId = eventId == null ? "" : eventId.trim();
        this.score = score;
        this.wouldMeetAgain = wouldMeetAgain;
        this.comment = comment == null ? "" : comment;
    }

    public String getRaterId() {
        return raterId;
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

    public String getComment() {
        return comment;
    }
}
