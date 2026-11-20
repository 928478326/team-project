package use_case.login;

/**
 * Output boundary for the Login use case.
 */
public interface LoginOutputBoundary {

    /**
     * Presents a successful login and activates the main application shell.
     *
     * @param outputData authenticated session metadata
     */
    void prepareSuccessView(LoginOutputData outputData);

    /**
     * Presents an authentication failure.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
