package use_case.join_event;

import java.time.Clock;
import java.util.Objects;

/**
 * Interactor implementing the Join Event use case (open-window check, capacity check, idempotency).
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class JoinEventInteractor implements JoinEventInputBoundary {

    private final JoinEventDataAccessInterface dataAccess;
    private final JoinEventOutputBoundary outputBoundary;
    private final Clock clock;

    public JoinEventInteractor(
            JoinEventDataAccessInterface dataAccess,
            JoinEventOutputBoundary outputBoundary,
            Clock clock) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess must not be null");
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void execute(JoinEventInputData inputData) {
        // TODO [Track B]:
        // 1. Validate userId and eventId non-blank.
        // 2. Call dataAccess.joinAtomically(userId, eventId, clock.instant()).
        // 3. Handle each Outcome:
        //    - JOINED / ALREADY_JOINED -> prepareSuccessView(...)
        //    - EVENT_NOT_FOUND / EVENT_CLOSED / FULL -> prepareFailView(...)
        outputBoundary.prepareFailView("TODO [Track B]: Implement JoinEventInteractor.execute");
    }
}
