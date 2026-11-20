package use_case.login;

/**
 * Input boundary for the Login use case.
 */
public interface LoginInputBoundary {

    /**
     * Authenticates a user using credentials in {@code inputData}.
     *
     * @param inputData login credentials
     */
    void execute(LoginInputData inputData);
}
