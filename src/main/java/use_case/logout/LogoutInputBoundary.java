package use_case.logout;

/**
 * Input boundary for the Logout use case.
 */
public interface LogoutInputBoundary {

    /**
     * Logs out the currently authenticated user.
     *
     * @param inputData logout request carrying the active username
     */
    void execute(LogoutInputData inputData);
}
