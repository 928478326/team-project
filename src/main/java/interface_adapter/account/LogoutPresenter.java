package interface_adapter.account;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.ViewManagerModel;
import java.util.Objects;
import use_case.logout.LogoutOutputBoundary;
import use_case.logout.LogoutOutputData;

/**
 * Presenter for account logout.
 * Clears {@link SessionViewModel} and switches back to {@link ViewManagerModel#ACCOUNT_VIEW}.
 * Feature views observe {@link SessionViewModel} to reset their own UI independently (OCP).
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class LogoutPresenter implements LogoutOutputBoundary {

    private final AccountViewModel accountViewModel;
    private final SessionViewModel sessionViewModel;
    private final ViewManagerModel viewManagerModel;

    public LogoutPresenter(
            AccountViewModel accountViewModel,
            SessionViewModel sessionViewModel,
            ViewManagerModel viewManagerModel) {
        this.accountViewModel =
                Objects.requireNonNull(accountViewModel, "accountViewModel must not be null");
        this.sessionViewModel =
                Objects.requireNonNull(sessionViewModel, "sessionViewModel must not be null");
        this.viewManagerModel =
                Objects.requireNonNull(viewManagerModel, "viewManagerModel must not be null");
    }

    @Override
    public void prepareSuccessView(LogoutOutputData outputData) {
        sessionViewModel.setState(SessionState.loggedOut());
        sessionViewModel.firePropertyChanged();

        AccountState state = accountViewModel.getState();
        state.setErrorMessage("");
        state.setStatusMessage(
                outputData.getLoggedOutUsername().isBlank()
                        ? "Logged out."
                        : "Logged out from " + outputData.getLoggedOutUsername() + ".");
        accountViewModel.firePropertyChanged();

        viewManagerModel.setState(ViewManagerModel.ACCOUNT_VIEW);
        viewManagerModel.firePropertyChanged();
    }
}
