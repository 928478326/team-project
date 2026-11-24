package interface_adapter.account;

import java.util.Objects;

import interface_adapter.SessionState;
import interface_adapter.SessionViewModel;
import interface_adapter.ViewManagerModel;
import use_case.signup.SignupOutputBoundary;
import use_case.signup.SignupOutputData;

/**
 * Presenter for account registration.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class SignupPresenter implements SignupOutputBoundary {

    private final AccountViewModel accountViewModel;
    private final SessionViewModel sessionViewModel;
    private final ViewManagerModel viewManagerModel;

    public SignupPresenter(
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
    public void prepareSuccessView(SignupOutputData outputData) {
        final AccountState state = accountViewModel.getState();
        state.setErrorMessage("");
        state.setStatusMessage("Registered as " + outputData.getUsername());
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
        final AccountState state = accountViewModel.getState();
        state.setStatusMessage("");
        state.setErrorMessage(errorMessage);
        accountViewModel.firePropertyChanged();
    }
}
