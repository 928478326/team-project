package use_case.create_event;

/**
 * Input boundary for publishing a new community event invitation.
 */
public interface CreateEventInputBoundary {

    /**
     * Validates and creates a new event.
     *
     * @param inputData raw event fields submitted by the user
     */
    void execute(CreateEventInputData inputData);
}
