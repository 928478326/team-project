package entity;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing a post-activity buddy rating submitted by {@code raterId}
 * for {@code rateeId} in the context of a shared {@code eventId}.
 */
public final class BuddyRating {

    /** Minimum allowed rating score. */
    public static final int MIN_SCORE = 1;
    /** Maximum allowed rating score. */
    public static final int MAX_SCORE = 5;
    /** Maximum allowed comment length in characters. */
    public static final int MAX_COMMENT_LENGTH = 300;

    private final String raterId;
    private final String rateeId;
    private final String eventId;
    private final int score;
    private final boolean wouldMeetAgain;
    private final String comment;
    private final Instant ratedAt;

    /**
     * Constructs an immutable {@code BuddyRating} while enforcing domain invariants.
     *
     * @param raterId user ID submitting the rating (non-blank)
     * @param rateeId user ID being rated (non-blank, must differ from {@code raterId})
     * @param eventId shared event ID (non-blank)
     * @param score satisfaction score from 1 to 5 inclusive
     * @param wouldMeetAgain whether the rater wants to hang out with the ratee again
     * @param comment optional feedback comment (max {@value #MAX_COMMENT_LENGTH} chars)
     * @param ratedAt UTC timestamp when the rating was recorded (non-null)
     */
    public BuddyRating(
            String raterId,
            String rateeId,
            String eventId,
            int score,
            boolean wouldMeetAgain,
            String comment,
            Instant ratedAt) {
        this.raterId = requireNonBlank(raterId, "raterId");
        this.rateeId = requireNonBlank(rateeId, "rateeId");
        this.eventId = requireNonBlank(eventId, "eventId");
        if (this.raterId.equals(this.rateeId)) {
            throw new IllegalArgumentException("Users cannot rate themselves.");
        }
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException(
                    "Rating score must be between " + MIN_SCORE + " and " + MAX_SCORE + ".");
        }
        String sanitizedComment = comment == null ? "" : comment.trim();
        if (sanitizedComment.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException(
                    "Comment must not exceed " + MAX_COMMENT_LENGTH + " characters.");
        }
        this.score = score;
        this.wouldMeetAgain = wouldMeetAgain;
        this.comment = sanitizedComment;
        this.ratedAt = Objects.requireNonNull(ratedAt, "ratedAt must not be null");
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

    public Instant getRatedAt() {
        return ratedAt;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value.trim();
    }
}
