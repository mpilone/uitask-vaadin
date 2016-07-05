
package org.mpilone.vaadin.uitask;

import java.util.Objects;
import java.util.concurrent.Future;

import com.vaadin.ui.UI;

/**
 * Provides methods to access the UI from another thread by properly
 * synchronizing the work to the UI thread/lock. In most cases the UIAccessor
 * will simply be the {@link UI} itself, however it is possible to configure
 * different implementations. The interface helps to abstract away the UI
 * implementation from the backend, non-UI code.
 *
 * @author mpilone
 */
public interface UIAccessor {

  /**
   * Queues the given work for execution by the UI. Refer to {@link UI#access(java.lang.Runnable)
   * }.
   *
   * @param r the work to queue
   *
   * @return a future representing the work to be completed
   */
  Future<Void> access(Runnable r);

  /**
   * Executes the given work immediately in the UI. This method blocks until the
   * work is complete. Refer to {@link UI#accessSynchronously(java.lang.Runnable)
   * }.
   *
   * @param r the work to execute
   */
  void accessSynchronously(Runnable r);

  /**
   * A simple implementation of a {@link UIAccessor} that uses a static, fixed
   * UI instance.
   */
  public static class Fixed implements UIAccessor {

    final private UI ui;

    /**
     * Constructs the accessor which will delegate to the given UI. The UI may
     * be detached at construction but it must be attached before calling any of
     * the access methods.
     *
     * @param ui the UI to delegate to
     *
     * @throws NullPointerException if the UI is null
     */
    public Fixed(UI ui) throws NullPointerException {
      Objects.requireNonNull(ui, "UI may not be null.");

      this.ui = ui;
    }

    @Override
    public Future<Void> access(Runnable r) {
      return ui.access(r);
    }

    @Override
    public void accessSynchronously(Runnable r) {
      ui.accessSynchronously(r);
    }

    /**
     * Returns the UI this accessor will use for all requests.
     *
     * @return the UI instance
     */
    protected UI getUI() {
      return ui;
    }
  }

  /**
   * An implementation of {@link UIAccessor} that uses the "current" UI at the
   * time of construction by calling {@link UI#getCurrent() }.
   */
  public static class Current extends Fixed {

    /**
     * Constructs the accessor which immediately looks up the current UI.
     */
    public Current() {
      super(UI.getCurrent());
    }
  }

}
