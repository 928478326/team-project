package use_case.create_event;

/**
 * Output boundary for creating a community event.
 */
public interface CreateEventOutputBoundary {

    /**
     * Presents a newly created event.
     *
     * @param outputData created event summary
     */
    void prepareSuccessView(CreateEventOutputData outputData);

    /**
     * Presents validation or persistence failure while retaining form draft.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
