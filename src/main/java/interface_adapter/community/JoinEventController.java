package interface_adapter.community;

import java.util.Objects;
import use_case.join_event.JoinEventInputBoundary;
import use_case.join_event.JoinEventInputData;

/**
 * Controller for joining a community event.
 *
 * <p><b>Owner: Track B (Community &amp; Calendar Lead)</b>
 */
public final class JoinEventController {

    private final JoinEventInputBoundary interactor;

    public JoinEventController(JoinEventInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    /**
     * Triggers an atomic join attempt for {@code userId} and {@code eventId}.
     *
     * @param userId active user ID
     * @param eventId selected event ID
     */
    public void execute(String userId, String eventId) {
        interactor.execute(new JoinEventInputData(userId, eventId));
    }
}
