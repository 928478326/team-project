package use_case.create_event;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Interactor implementing the Create Event use case.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class CreateEventInteractor implements CreateEventInputBoundary {

    /** Expected local date-time pattern in the configured Toronto zone. */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private final CreateEventDataAccessInterface dataAccess;
    private final CreateEventOutputBoundary outputBoundary;
    private final Clock clock;
    private final ZoneId zoneId;

    public CreateEventInteractor(
            CreateEventDataAccessInterface dataAccess,
            CreateEventOutputBoundary outputBoundary,
            Clock clock,
            ZoneId zoneId) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId must not be null");
    }

    @Override
    public void execute(CreateEventInputData inputData) {
        // TODO [Track B]:
        // 1. Validate creatorId non-blank, communityExists, title non-blank,
        //    capacity integer in [Event.MIN_CAPACITY, Event.MAX_CAPACITY].
        // 2. Parse startsAtLocalText using DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
        //    in zoneId -> Instant, and verify startsAt.isAfter(clock.instant()).
        // 3. Construct Event with UUID, call dataAccess.saveEvent(event),
        //    and notify outputBoundary.prepareSuccessView(...).
        outputBoundary.prepareFailView("TODO [Track B]: Implement CreateEventInteractor.execute");
    }
}
