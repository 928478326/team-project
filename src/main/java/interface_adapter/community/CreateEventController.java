package interface_adapter.community;

import java.util.Objects;
import use_case.create_event.CreateEventInputBoundary;
import use_case.create_event.CreateEventInputData;

/**
 * Controller for publishing a new community event.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CreateEventController {

    private final CreateEventInputBoundary interactor;

    public CreateEventController(CreateEventInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    /**
     * Packages raw event form inputs and invokes the Create Event use case.
     *
     * @param creatorId logged-in creator user ID
     * @param communityId chosen community ID
     * @param title event title
     * @param description event description
     * @param capacityText raw capacity text
     * @param startsAtLocalText local start time string ({@code yyyy-MM-dd HH:mm})
     */
    public void execute(
            String creatorId,
            String communityId,
            String title,
            String description,
            String capacityText,
            String startsAtLocalText) {
        CreateEventInputData inputData =
                new CreateEventInputData(
                        creatorId, communityId, title, description, capacityText, startsAtLocalText);
        interactor.execute(inputData);
    }
}
