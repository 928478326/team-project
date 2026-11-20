package use_case.logout;

import java.util.Objects;

/**
 * Interactor implementing the Logout use case.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class LogoutInteractor implements LogoutInputBoundary {

    private final LogoutOutputBoundary outputBoundary;

    public LogoutInteractor(LogoutOutputBoundary outputBoundary) {
        this.outputBoundary =
                Objects.requireNonNull(outputBoundary, "outputBoundary must not be null");
    }

    @Override
    public void execute(LogoutInputData inputData) {
        // TODO [Track A]: Pass the logged-out username via LogoutOutputData to outputBoundary.
        outputBoundary.prepareSuccessView(new LogoutOutputData(inputData.getUsername()));
    }
}
