package use_case.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import data_access.in_memory.InMemoryCampusBuddyStore;
import data_access.in_memory.InMemoryUserDataAccess;
import entity.CommonUserFactory;
import entity.PasswordHasher;
import entity.User;
import interface_adapter.SessionViewModel;
import interface_adapter.ViewManagerModel;
import interface_adapter.account.AccountViewModel;
import interface_adapter.account.LoginPresenter;
import interface_adapter.account.LogoutPresenter;
import interface_adapter.account.SignupPresenter;
import use_case.login.LoginInputData;
import use_case.login.LoginInteractor;
import use_case.logout.LogoutInputData;
import use_case.logout.LogoutInteractor;
import use_case.signup.SignupInputData;
import use_case.signup.SignupInteractor;

class AccountFlowTest {
    private final InMemoryUserDataAccess users = new InMemoryUserDataAccess(new InMemoryCampusBuddyStore());
    private final AccountViewModel account = new AccountViewModel();
    private final SessionViewModel session = new SessionViewModel();
    private final ViewManagerModel pages = new ViewManagerModel();
    private final PasswordHasher hasher = password -> "test-hash-" + password;
    private final SignupPresenter signupPresenter = new SignupPresenter(account, session, pages);
    private final LoginPresenter loginPresenter = new LoginPresenter(account, session, pages);
    private final SignupInteractor signup = new SignupInteractor(
            users, signupPresenter, new CommonUserFactory(), hasher);
    private final LoginInteractor login = new LoginInteractor(users, loginPresenter, hasher);
    private final LogoutInteractor logout = new LogoutInteractor(new LogoutPresenter(account, session, pages));

    @Test
    void registerSignOutAndReturnToTheSameAccount() {
        signup.execute(new SignupInputData("  Maple  ", "secret", "secret"));
        final User saved = users.findByUsername("maple").orElseThrow();
        assertEquals("maple", saved.getDisplayName());
        assertNotEquals("secret", saved.getPasswordHash());
        assertEquals(saved.getUserId(), session.getState().getUserId());
        assertEquals(ViewManagerModel.MAIN_SHELL_VIEW, pages.getState());

        logout.execute(new LogoutInputData("maple"));
        assertEquals("", session.getState().getUserId());
        assertEquals(ViewManagerModel.ACCOUNT_VIEW, pages.getState());

        signup.execute(new SignupInputData("MAPLE", "other123", "other123"));
        assertFalse(account.getState().getErrorMessage().isBlank());
        assertFalse(session.getState().isLoggedIn());

        login.execute(new LoginInputData(" maple ", "secret"));
        assertEquals(saved.getUserId(), session.getState().getUserId());
        assertEquals(ViewManagerModel.MAIN_SHELL_VIEW, pages.getState());
        assertTrue(account.getState().getErrorMessage().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"'',secret1,secret1", "maple,short,short", "maple,'      ','      '", "maple,secret1,different"})
    void invalidSignupKeepsTheAccountFormAndCreatesNoUser(String username, String password, String confirmation) {
        signup.execute(new SignupInputData(username, password, confirmation));
        assertFalse(account.getState().getErrorMessage().isBlank());
        assertFalse(session.getState().isLoggedIn());
        assertEquals(ViewManagerModel.ACCOUNT_VIEW, pages.getState());
        assertTrue(users.findByUsername(username.trim()).isEmpty());
    }

    @Test
    void userIdCollisionPreservesTheExistingAccount() {
        final CommonUserFactory factory = new CommonUserFactory();
        users.saveUser(factory.create("same-id", "maple", "original-hash"));
        assertThrows(IllegalStateException.class,
                () -> users.saveUser(factory.create("same-id", "elm", "different-hash")));
        assertEquals("original-hash", users.findByUsername("maple").orElseThrow().getPasswordHash());
        assertTrue(users.findByUsername("elm").isEmpty());
    }

    @Test
    void failedAccountWriteOrReadLeavesStudentSignedOut() {
        final SignupInteractor offlineSignup = new SignupInteractor(user -> {
            throw new IllegalStateException("test write failed");
        }, signupPresenter, new CommonUserFactory(), hasher);
        offlineSignup.execute(new SignupInputData("maple", "secret1", "secret1"));
        assertEquals("Could not save the account. Please try again.", account.getState().getErrorMessage());
        assertFalse(session.getState().isLoggedIn());

        final LoginInteractor offlineLogin = new LoginInteractor(username -> {
            throw new IllegalStateException("test read failed");
        }, loginPresenter, hasher);
        offlineLogin.execute(new LoginInputData("maple", "secret1"));
        assertEquals("Could not load the account. Please try again.", account.getState().getErrorMessage());
        assertFalse(session.getState().isLoggedIn());
    }

    @ParameterizedTest
    @CsvSource({"unknown,secret1", "maple,incorrect", "'',secret1", "maple,''"})
    void rejectedLoginLeavesStudentSignedOut(String username, String password) {
        signup.execute(new SignupInputData("maple", "secret1", "secret1"));
        logout.execute(new LogoutInputData("maple"));
        login.execute(new LoginInputData(username, password));
        assertFalse(account.getState().getErrorMessage().isBlank());
        assertFalse(session.getState().isLoggedIn());
        assertEquals(ViewManagerModel.ACCOUNT_VIEW, pages.getState());
    }
}
