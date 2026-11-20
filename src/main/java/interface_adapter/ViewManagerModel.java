package interface_adapter;

/**
 * Observable ViewModel tracking which top-level screen is currently active
 * (e.g., {@link #ACCOUNT_VIEW} or {@link #MAIN_SHELL_VIEW}).
 */
public final class ViewManagerModel extends ViewModel<String> {

    public static final String ACCOUNT_VIEW = "account";
    public static final String MAIN_SHELL_VIEW = "main_shell";

    public ViewManagerModel() {
        super("view_manager");
        setState(ACCOUNT_VIEW);
    }
}
