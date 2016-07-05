
package org.mpilone.vaadin.uitask;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.UIDetachedException;

/**
 * An implementation of {@link UITask} that provides convenient properties for
 * monitoring the progress of a background task. The properties can be bound to
 * the UI and will be updated safely in the UI thread/lock. While useful on it's
 * own, it is more an example of how the {@link UITask} can be used to update
 * properties or data that is bound to a UI.
 *
 * @param <T> the return type of the background work performed by the task
 *
 * @author mpilone
 */
public abstract class ProgressUITask<T> extends UITask<T> {

  private final ObjectProperty<Double> progress = new ObjectProperty<>(0.0,
      Double.class);
  private final ObjectProperty<Double> total = new ObjectProperty<>(0.0,
      Double.class);
  private final ObjectProperty<String> message = new ObjectProperty<>("",
      String.class);

  /**
   * Constructs the progress task.
   *
   * @param uiAccessor the UI accessor used to synchronize to the UI for updates
   */
  public ProgressUITask(UIAccessor uiAccessor) {
    super(uiAccessor);
  }

  /**
   * Updates the total property value by synchronizing the update with the UI.
   *
   * @param value the new value of the total property
   */
  protected void updateTotal(final double value) {
    try {
      getUIAccessor().access(() -> {
        total.setValue(value);
      });
    }
    catch (UIDetachedException ex) {
      detachedUpdate(ex);
    }
  }

  /**
   * Updates the progress property value by synchronizing the update with the
   * UI.
   *
   * @param value the new value of the progress property
   */
  protected void updateProgress(final double value) {
    getUIAccessor().access(() -> {
      progress.setValue(value);
    });
  }

  /**
   * Updates the message property value by synchronizing the update with the UI.
   *
   * @param value the new value of the message property
   */
  protected void updateMessage(final String value) {
    getUIAccessor().access(() -> {
      message.setValue(value);
    });
  }

  /**
   * Returns the progress property which may be safely bound to a UI component.
   * The progress is usually a value from 0 to 1.
   *
   * @return the progress property
   */
  public ObjectProperty<Double> getProgress() {
    return progress;
  }

  /**
   * Returns the message property which may be safely bound to a UI component.
   * The message is usually a status message to display to the user to monitor a
   * background task.
   *
   * @return the message property
   */
  public ObjectProperty<String> getMessage() {
    return message;
  }

  /**
   * Returns the total property which may be safely bound to a UI component. The
   * total may represent the total amount of work to be done such as chunks to
   * process or bytes to download.
   *
   * @return the total property
   */
  public ObjectProperty<Double> getTotal() {
    return total;
  }

  /**
   * Executed in the background thread if the UI was detached at the time any
   * update method was called. In most cases this method isn't needed but it can
   * be used by subclasses to cleanup. The UI should not be accessed in this
   * method.
   *
   * @param ex the exception that was raised when an update method was called.
   */
  protected void detachedUpdate(UIDetachedException ex) {
    // no-op
  }

}
