package interface_adapter.buddy_rating;

import java.util.function.Consumer;

import interface_adapter.ViewModel;

/**
 * Observable rating state scoped to the current signed-in session.
 */
public final class BuddyRatingViewModel extends ViewModel<BuddyRatingState> {

    public static final String VIEW_NAME = "buddy_rating";
    public static final String ROWS_LOADED = "rowsLoaded";

    private final ThreadLocal<BuddyRatingState> requestSession = new ThreadLocal<>();

    public BuddyRatingViewModel() {
        super(VIEW_NAME);
        clearSession();
    }

    /**
     * Drops the previous session's rows, messages and outstanding results.
     */
    public synchronized void clearSession() {
        setState(new BuddyRatingState());
    }

    /**
     * Binds synchronous presenter callbacks to the session that started a background request.
     *
     * @param request controller call to perform
     * @return request that cannot publish into a later session
     */
    public synchronized Runnable forCurrentSession(Runnable request) {
        final BuddyRatingState session = getState();
        return () -> {
            requestSession.set(session);
            try {
                request.run();
            }
            finally {
                requestSession.remove();
            }
        };
    }

    synchronized void present(String property, Consumer<BuddyRatingState> update) {
        final BuddyRatingState owner = requestSession.get();
        if (owner == null || owner == getState()) {
            update.accept(getState());
            firePropertyChanged(property);
        }
    }
}
