package use_case.view_rateable_buddies;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable output data for viewing buddies eligible for rating.
 */
public final class ViewRateableBuddiesOutputData {

    private final List<RateableBuddyItem> items;

    public ViewRateableBuddiesOutputData(List<RateableBuddyItem> items) {
        this.items = items == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(items));
    }

    public List<RateableBuddyItem> getItems() {
        return items;
    }

    /**
     * Immutable DTO describing one (buddy, shared event) pair and any prior rating submitted by me.
     */
    public static final class RateableBuddyItem {

        private final String eventId;
        private final String eventTitle;
        private final Instant eventStartsAt;
        private final String buddyUserId;
        private final String buddyDisplayName;
        private final boolean alreadyRated;
        private final int existingScore;
        private final boolean existingWouldMeetAgain;
        private final String existingComment;

        public RateableBuddyItem(
                String eventId,
                String eventTitle,
                Instant eventStartsAt,
                String buddyUserId,
                String buddyDisplayName,
                boolean alreadyRated,
                int existingScore,
                boolean existingWouldMeetAgain,
                String existingComment) {
            this.eventId = eventId;
            this.eventTitle = eventTitle;
            this.eventStartsAt = eventStartsAt;
            this.buddyUserId = buddyUserId;
            this.buddyDisplayName = buddyDisplayName;
            this.alreadyRated = alreadyRated;
            this.existingScore = existingScore;
            this.existingWouldMeetAgain = existingWouldMeetAgain;
            this.existingComment = existingComment == null ? "" : existingComment;
        }

        public String getEventId() {
            return eventId;
        }

        public String getEventTitle() {
            return eventTitle;
        }

        public Instant getEventStartsAt() {
            return eventStartsAt;
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
