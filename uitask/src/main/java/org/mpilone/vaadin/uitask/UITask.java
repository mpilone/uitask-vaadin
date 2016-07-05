
package org.mpilone.vaadin.uitask;

import java.util.concurrent.*;

import com.vaadin.ui.UIDetachedException;

/**
 * <p>
 * A task that can be run on a background thread via an {@link Executor} and
 * then complete the work safely in the UI thread/lock. Vaadin is not
 * thread-safe, therefore all UI modifications must be done after obtaining the
 * UI lock. This class attempts to simplify that process by implementing
 * {@link Future} and exposing methods that are safely called in the UI thread.
 * </p>
 * <p>
 * The common use case is to subclass UITask and implement the {@link #runInBackground()
 * } and {@link #done()} methods. The former being called in the background
 * thread and the latter being called in the UI thread/lock. The task can then
 * be executed by creating a separate thread or using an {@link Executor}
 * implementation such as a thread-pool.
 * </p>
 * <p>
 * This class is inspired by Swing's SwingWorker and JavaFX's Task.
 * </p>
 *
 * @param <T> the return type of the background work performed by the task
 *
 * @author mpilone
 */
public abstract class UITask<T> implements RunnableFuture<T> {

  private final FutureTask<T> task;
  private final UIAccessor uIAccessor;

  /**
   * Constructs the task. The given UI accessor will be used to synchronize to
   * the UI thread/lock when performing UI updates.
   *
   * @param uiAccessor the UI accessor for lock synchronization
   */
  public UITask(UIAccessor uiAccessor) {
    this.task = new FutureTask<>(this::runInBackground);
    this.uIAccessor = uiAccessor;
  }

  /**
   * Executed in the background thread. Subclasses must implement this method to
   * perform any background logic. The UI may not be modified directly in this
   * method, however the {@link UIAccessor} may be used to submit UI work. In
   * most cases this method should stage work or calculate results that will be
   * applied in the {@link #done() } method in the UI lock.
   *
   * @return the results of the background work
   */
  abstract protected T runInBackground();

  @Override
  public void run() {
    task.run();

    try {
      uIAccessor.access(this::done);
    }
    catch (UIDetachedException ex) {
      // Ignore. This can happen if the UI was detached while executing
      // in the background. If there is no UI, there is no way to execute
      // the done method in the UI thread/lock.
      detachedDone(ex);
    }
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return task.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return task.isCancelled();
  }

  @Override
  public boolean isDone() {
    return task.isDone();
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    return task.get();
  }

  @Override
  public T get(long timeout, TimeUnit unit) throws InterruptedException,
      ExecutionException, TimeoutException {
    return task.get(timeout, unit);
  }

  /**
   * Returns the UI accessor that can be used to submit work for execution in
   * the UI thread/lock. In most cases this method isn't required because the {@link #done()
   * } method will be called automatically.
   *
   * @return the UI accessor
   */
  public UIAccessor getUIAccessor() {
    return uIAccessor;
  }

  /**
   * Executed in the UI thread/lock after the background work is done. This
   * method should be implemented in subclasses to apply any changes/data
   * calculated in the background to the UI. This method will not be called if
   * the UI was detached before the background work completed (see {@link #detachedDone(com.vaadin.ui.UIDetachedException)
   * }. The {@link Future} methods on this class can be used to determine if the
   * task was canceled or get the result of the background work.
   */
  protected void done() {
    // no op
  }

  /**
   * Executed in the background thread if the UI was detached at the time the {@link #done()
   * } method was called. In most cases this method isn't needed but it can be
   * used by subclasses to cleanup. The UI should not be accessed in this
   * method.
   *
   * @param ex the exception that was raised when {@link #done()} was called.
   */
  protected void detachedDone(UIDetachedException ex) {
    // no op
  }

}
