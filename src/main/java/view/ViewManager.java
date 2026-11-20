package view;

import interface_adapter.ViewManagerModel;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Observes {@link ViewManagerModel} and switches the visible top-level screen inside the root
 * {@link CardLayout} container.
 */
public final class ViewManager implements PropertyChangeListener {

    private final JPanel views;
    private final CardLayout cardLayout;

    public ViewManager(
            JPanel views, CardLayout cardLayout, ViewManagerModel viewManagerModel) {
        this.views = Objects.requireNonNull(views, "views must not be null");
        this.cardLayout = Objects.requireNonNull(cardLayout, "cardLayout must not be null");
        Objects.requireNonNull(viewManagerModel, "viewManagerModel must not be null");
        viewManagerModel.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!ViewManagerModel.STATE_PROPERTY.equals(evt.getPropertyName())) {
            return;
        }
        String targetViewName = (String) evt.getNewValue();
        if (targetViewName == null || targetViewName.isBlank()) {
            return;
        }
        Runnable switchTask = () -> cardLayout.show(views, targetViewName);
        if (SwingUtilities.isEventDispatchThread()) {
            switchTask.run();
        } else {
            SwingUtilities.invokeLater(switchTask);
        }
    }
}
