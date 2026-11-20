package use_case.signup;

/**
 * Input boundary for the Signup (register new student account) use case.
 */
public interface SignupInputBoundary {

    /**
     * Executes account registration.
     *
     * @param inputData registration credentials supplied by the controller
     */
    void execute(SignupInputData inputData);
}
