package interface_adapter.buddy_rating;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UI state for the Buddy Rating view.
 *
 * <p><b>Owner: Track C (Course Matching &amp; Buddy Rating Lead)</b>
 */
public final class BuddyRatingState {

    private List<RateableBuddyRow> items = Collections.emptyList();
    private String statusMessage = "";
    private String errorMessage = "";

    public List<RateableBuddyRow> getItems() {
        return items;
    }

    public void setItems(List<RateableBuddyRow> items) {
        this.items = items == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(items));
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage == null ? "" : statusMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage == null ? "" : errorMessage;
    }

    /**
     * Formatted row describing one co-participant eligible for rating.
     */
    public static final class RateableBuddyRow {

        private final String eventId;
        private final String eventTitle;
        private final String eventTimeDisplay;
        private final String buddyUserId;
        private final String buddyDisplayName;
        private final boolean alreadyRated;
        private final int existingScore;
        private final boolean existingWouldMeetAgain;
        private final String existingComment;

        public RateableBuddyRow(
                String eventId,
                String eventTitle,
                String eventTimeDisplay,
                String buddyUserId,
                String buddyDisplayName,
                boolean alreadyRated,
                int existingScore,
                boolean existingWouldMeetAgain,
                String existingComment) {
            this.eventId = eventId;
            this.eventTitle = eventTitle;
            this.eventTimeDisplay = eventTimeDisplay;
            this.buddyUserId = buddyUserId;
            this.buddyDisplayName = buddyDisplayName;
            this.alreadyRated = alreadyRated;
            this.existingScore = existingScore;
            this.existingWouldMeetAgain = existingWouldMeetAgain;
            this.existingComment = existingComment;
        }

        public String getEventId() {
            return eventId;
        }

        public String getEventTitle() {
            return eventTitle;
        }

        public String getEventTimeDisplay() {
            return eventTimeDisplay;
        }

        public String getBuddyUserId() {
            return buddyUserId;
        }

        public String getBuddyDisplayName() {
            return buddyDisplayName;
        }

        public boolean isAlreadyRated() {
            return alreadyRated;
        }

        public int getExistingScore() {
            return existingScore;
        }

        public boolean isExistingWouldMeetAgain() {
            return existingWouldMeetAgain;
        }

        public String getExistingComment() {
            return existingComment;
        }
    }
}
