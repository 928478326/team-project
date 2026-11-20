package interface_adapter.account;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.ViewManagerModel;
import java.util.Objects;
import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;

/**
 * Presenter for account login.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class LoginPresenter implements LoginOutputBoundary {

    private final AccountViewModel accountViewModel;
    private final SessionViewModel sessionViewModel;
    private final ViewManagerModel viewManagerModel;

    public LoginPresenter(
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
    public void prepareSuccessView(LoginOutputData outputData) {
        // TODO [Track A]: Refine state clearing and status messaging as needed.
        AccountState state = accountViewModel.getState();
        state.setErrorMessage("");
        state.setStatusMessage("");
        accountViewModel.firePropertyChanged();

        sessionViewModel.setState(
                SessionState.loggedIn(
                        outputData.getUserId(),
                        outputData.getUsername(),
                        outputData.getDisplayName()));
        sessionViewModel.firePropertyChanged();

        viewManagerModel.setState(ViewManagerModel.MAIN_SHELL_VIEW);
        viewManagerModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        AccountState state = accountViewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        accountViewModel.firePropertyChanged();
    }
}
