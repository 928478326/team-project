package interface_adapter.account;

import java.util.Objects;
import use_case.logout.LogoutInputBoundary;
import use_case.logout.LogoutInputData;

/**
 * Controller for account logout.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class LogoutController {

    private final LogoutInputBoundary interactor;

    public LogoutController(LogoutInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor, "interactor must not be null");
    }

    /**
     * Triggers logout for the specified username.
     *
     * @param username active username read by the View from SessionViewModel
     */
    public void execute(String username) {
        interactor.execute(new LogoutInputData(username));
    }
}
