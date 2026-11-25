package use_case.join_event;

import java.time.Clock;
import java.util.Objects;

import use_case.join_event.JoinEventDataAccessInterface.JoinAttemptResult;

/**
 * Interactor implementing the Join Event use case (open-window check, capacity check, idempotency).
 *
 * <p>The open-window, capacity, and duplicate checks all happen inside
 * {@link JoinEventDataAccessInterface#joinAtomically}, so two students racing for the last seat
 * can never both get it. Joining an event twice is reported as a success, not an error.
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
        if (inputData.getUserId().isEmpty()) {
            outputBoundary.prepareFailView("Sign in before joining an event.");
        }
        else if (inputData.getEventId().isEmpty()) {
            outputBoundary.prepareFailView("Choose an event to join.");
        }
        else {
            attemptJoin(inputData.getUserId(), inputData.getEventId());
        }
    }

    private void attemptJoin(String userId, String eventId) {
        JoinAttemptResult result;
        try {
            result = dataAccess.joinAtomically(userId, eventId, clock.instant());
        }
        catch (IllegalStateException ex) {
            result = null;
        }
        if (result == null || result.getOutcome() == null) {
            outputBoundary.prepareFailView("Could not join the event. Please try again.");
        }
        else {
            present(eventId, result);
        }
    }

    private void present(String eventId, JoinAttemptResult result) {
        final int joined = result.getJoinedCount();
        final int capacity = result.getCapacity();
        switch (result.getOutcome()) {
            case JOINED:
                outputBoundary.prepareSuccessView(new JoinEventOutputData(
                        eventId, false, joined, capacity,
                        "You joined the event (" + joined + "/" + capacity + " spots filled)."));
                break;
            case ALREADY_JOINED:
                outputBoundary.prepareSuccessView(new JoinEventOutputData(
                        eventId, true, joined, capacity, "You have already joined this event."));
                break;
            case FULL:
                outputBoundary.prepareFailView(
                        "This event is full (" + joined + "/" + capacity + " spots filled).");
                break;
            case EVENT_CLOSED:
                outputBoundary.prepareFailView("This event has already started, so joining is closed.");
                break;
            case EVENT_NOT_FOUND:
            default:
                outputBoundary.prepareFailView("This event no longer exists.");
                break;
        }
    }
}
