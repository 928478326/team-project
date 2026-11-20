package use_case.browse_events;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable output data for browsing community events.
 * Contains pure DTOs with no {@code entity} types exposed across the outer boundary.
 */
public final class BrowseEventsOutputData {

    private final String selectedCommunityId;
    private final List<EventItem> events;

    public BrowseEventsOutputData(String selectedCommunityId, List<EventItem> events) {
        this.selectedCommunityId = selectedCommunityId;
        this.events = events == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(events));
    }

    public String getSelectedCommunityId() {
        return selectedCommunityId;
    }

    public List<EventItem> getEvents() {
        return events;
    }

    /**
     * Immutable DTO describing one event row in the community feed.
     */
    public static final class EventItem {

        private final String eventId;
        private final String communityId;
        private final String communityName;
        private final String creatorDisplayName;
        private final String title;
        private final String description;
        private final int capacity;
        private final int joinedCount;
        private final Instant startsAt;
        private final boolean joinedByCurrentUser;
        private final boolean openForJoin;

        public EventItem(
                String eventId,
                String communityId,
                String communityName,
                String creatorDisplayName,
                String title,
                String description,
                int capacity,
                int joinedCount,
                Instant startsAt,
                boolean joinedByCurrentUser,
                boolean openForJoin) {
            this.eventId = eventId;
            this.communityId = communityId;
            this.communityName = communityName;
            this.creatorDisplayName = creatorDisplayName;
            this.title = title;
            this.description = description;
            this.capacity = capacity;
            this.joinedCount = joinedCount;
            this.startsAt = startsAt;
            this.joinedByCurrentUser = joinedByCurrentUser;
            this.openForJoin = openForJoin;
        }

        public String getEventId() {
            return eventId;
        }

        public String getCommunityId() {
            return communityId;
        }

        public String getCommunityName() {
            return communityName;
        }

        public String getCreatorDisplayName() {
            return creatorDisplayName;
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

        public int getJoinedCount() {
            return joinedCount;
        }

        public Instant getStartsAt() {
            return startsAt;
        }

        public boolean isJoinedByCurrentUser() {
            return joinedByCurrentUser;
        }

        public boolean isOpenForJoin() {
            return openForJoin;
        }
    }
}
