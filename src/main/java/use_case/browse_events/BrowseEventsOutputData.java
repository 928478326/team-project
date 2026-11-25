package use_case.browse_events;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable output data for browsing community events.
 * Contains pure DTOs with no {@code entity} types exposed across the outer boundary.
 */
public final class BrowseEventsOutputData {

    private final String selectedCommunityId;
    private final List<EventItem> events;
    private final List<CommunityOption> communities;

    public BrowseEventsOutputData(String selectedCommunityId, List<EventItem> events) {
        this(selectedCommunityId, events, Collections.emptyList());
    }

    /**
     * Creates the output with the communities the user can filter by or post to.
     *
     * @param selectedCommunityId the filter that was applied ({@code ALL} or a community ID)
     * @param events ordered event rows
     * @param communities every community, in display order
     */
    public BrowseEventsOutputData(
            String selectedCommunityId, List<EventItem> events, List<CommunityOption> communities) {
        this.selectedCommunityId = selectedCommunityId;
        this.events = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNullElse(events, Collections.emptyList())));
        this.communities = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNullElse(communities, Collections.emptyList())));
    }

    public String getSelectedCommunityId() {
        return selectedCommunityId;
    }

    public List<EventItem> getEvents() {
        return events;
    }

    public List<CommunityOption> getCommunities() {
        return communities;
    }

    /**
     * Immutable DTO naming one community, so outer layers never need the {@code Community} entity.
     */
    public static final class CommunityOption {

        private final String communityId;
        private final String name;

        public CommunityOption(String communityId, String name) {
            this.communityId = communityId;
            this.name = name;
        }

        public String getCommunityId() {
            return communityId;
        }

        public String getName() {
            return name;
        }
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
