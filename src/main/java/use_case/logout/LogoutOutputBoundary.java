package use_case.logout;

/**
 * Output boundary for the Logout use case.
 */
public interface LogoutOutputBoundary {

    /**
     * Presents the post-logout state and returns to the account view.
     *
     * @param outputData logout confirmation payload
     */
    void prepareSuccessView(LogoutOutputData outputData);
}
