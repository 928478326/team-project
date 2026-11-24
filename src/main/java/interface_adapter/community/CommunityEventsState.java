package interface_adapter.community;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UI state for the Community Events view.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CommunityEventsState {

    private String selectedCommunityId = "ALL";
    private List<EventRow> events = Collections.emptyList();
    private List<CommunityOption> communities = Collections.emptyList();
    private String statusMessage = "";
    private String errorMessage = "";
    private boolean clearCreateForm;

    public String getSelectedCommunityId() {
        return selectedCommunityId;
    }

    public void setSelectedCommunityId(String selectedCommunityId) {
        this.selectedCommunityId = selectedCommunityId == null ? "ALL" : selectedCommunityId;
    }

    public List<EventRow> getEvents() {
        return events;
    }

    public void setEvents(List<EventRow> events) {
        this.events = events == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(events));
    }

    public List<CommunityOption> getCommunities() {
        return communities;
    }

    public void setCommunities(List<CommunityOption> communities) {
        this.communities = communities == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(communities));
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

    public boolean isClearCreateForm() {
        return clearCreateForm;
    }

    public void setClearCreateForm(boolean clearCreateForm) {
        this.clearCreateForm = clearCreateForm;
    }

    /**
     * One community choice for the filter and create-event drop-downs.
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

        /** Shown by Swing combo boxes. */
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Formatted view-model row representing one community event card/row.
     */
    public static final class EventRow {

        private final String eventId;
        private final String communityId;
        private final String communityName;
        private final String creatorDisplayName;
        private final String title;
        private final String description;
        private final String startsAtDisplay;
        private final int joinedCount;
        private final int capacity;
        private final boolean joinedByCurrentUser;
        private final boolean openForJoin;

        public EventRow(
                String eventId,
                String communityId,
                String communityName,
                String creatorDisplayName,
                String title,
                String description,
                String startsAtDisplay,
                int joinedCount,
                int capacity,
                boolean joinedByCurrentUser,
                boolean openForJoin) {
            this.eventId = eventId;
            this.communityId = communityId;
            this.communityName = communityName;
            this.creatorDisplayName = creatorDisplayName;
            this.title = title;
            this.description = description;
            this.startsAtDisplay = startsAtDisplay;
            this.joinedCount = joinedCount;
            this.capacity = capacity;
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

        public String getStartsAtDisplay() {
            return startsAtDisplay;
        }

        public int getJoinedCount() {
            return joinedCount;
        }

        public int getCapacity() {
            return capacity;
        }

        public boolean isJoinedByCurrentUser() {
            return joinedByCurrentUser;
        }

        public boolean isOpenForJoin() {
            return openForJoin;
        }

        public boolean isFull() {
            return joinedCount >= capacity;
        }
    }
}
