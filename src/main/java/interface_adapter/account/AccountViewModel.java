package interface_adapter.account;

import interface_adapter.ViewManagerModel;
import interface_adapter.ViewModel;

/**
 * ViewModel for the Account (Login / Signup) view.
 *
 * <p><b>Owner: Track A (Data Platform &amp; Auth Lead)</b>
 */
public final class AccountViewModel extends ViewModel<AccountState> {

    public AccountViewModel() {
        super(ViewManagerModel.ACCOUNT_VIEW);
        setState(new AccountState());
    }
}
