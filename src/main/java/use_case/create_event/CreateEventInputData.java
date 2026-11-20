package use_case.create_event;

/**
 * Immutable input data for creating a community event.
 * Keeps raw strings for capacity and date-time so business validation remains inside the Interactor.
 */
public final class CreateEventInputData {

    private final String creatorId;
    private final String communityId;
    private final String title;
    private final String description;
    private final String capacityText;
    private final String startsAtLocalText;

    public CreateEventInputData(
            String creatorId,
            String communityId,
            String title,
            String description,
            String capacityText,
            String startsAtLocalText) {
        this.creatorId = creatorId == null ? "" : creatorId.trim();
        this.communityId = communityId == null ? "" : communityId.trim();
        this.title = title == null ? "" : title;
        this.description = description == null ? "" : description;
        this.capacityText = capacityText == null ? "" : capacityText.trim();
        this.startsAtLocalText = startsAtLocalText == null ? "" : startsAtLocalText.trim();
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getCommunityId() {
        return communityId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCapacityText() {
        return capacityText;
    }

    public String getStartsAtLocalText() {
        return startsAtLocalText;
    }
}
