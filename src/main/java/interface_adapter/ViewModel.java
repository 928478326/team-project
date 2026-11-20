package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

/**
 * Generic observable ViewModel base class implementing the Observer pattern via
 * {@link PropertyChangeSupport} (matching CSC207 Clean Architecture guidelines).
 *
 * @param <T> state object type held by this ViewModel
 */
public class ViewModel<T> {

    /** Default property name fired when state changes. */
    public static final String STATE_PROPERTY = "state";

    private final String viewName;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private T state;

    public ViewModel(String viewName) {
        this.viewName = Objects.requireNonNull(viewName, "viewName must not be null");
    }

    public String getViewName() {
        return viewName;
    }

    public T getState() {
        return state;
    }

    public void setState(T state) {
        this.state = state;
    }

    /**
     * Notifies all registered observers that {@link #STATE_PROPERTY} has been updated.
     */
    public void firePropertyChanged() {
        this.support.firePropertyChange(STATE_PROPERTY, null, this.state);
    }

    /**
     * Notifies all registered observers with a specific property name.
     *
     * @param propertyName custom property event name
     */
    public void firePropertyChanged(String propertyName) {
        this.support.firePropertyChange(propertyName, null, this.state);
    }

    /**
     * Registers a view or listener to observe state changes.
     *
     * @param listener observer to notify
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }
}
