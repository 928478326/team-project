package use_case.join_event;

/**
 * Input boundary for joining a community event.
 */
public interface JoinEventInputBoundary {

    /**
     * Attempts to enroll a user in an open event while respecting capacity and idempotency.
     *
     * @param inputData user ID and target event ID
     */
    void execute(JoinEventInputData inputData);
}
