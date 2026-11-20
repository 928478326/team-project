package use_case.join_event;

/**
 * Output boundary for joining a community event.
 */
public interface JoinEventOutputBoundary {

    /**
     * Presents a successful (or idempotent already-joined) join outcome.
     *
     * @param outputData updated enrollment counts and status
     */
    void prepareSuccessView(JoinEventOutputData outputData);

    /**
     * Presents a failure such as event not found, already started, or at capacity.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
