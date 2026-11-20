package use_case.signup;

/**
 * Output boundary for the Signup use case.
 */
public interface SignupOutputBoundary {

    /**
     * Presents a newly registered account.
     *
     * @param outputData created user summary
     */
    void prepareSuccessView(SignupOutputData outputData);

    /**
     * Presents a validation or persistence failure.
     *
     * @param errorMessage user-facing error message
     */
    void prepareFailView(String errorMessage);
}
