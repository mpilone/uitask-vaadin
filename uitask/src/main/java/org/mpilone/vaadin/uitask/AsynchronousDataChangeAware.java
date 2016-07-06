
package org.mpilone.vaadin.uitask;

/**
 * <p>
 * A component that can listen for data changes that happen asynchronously,
 * outside of the normal UI lock. These data changes must be synchronized with
 * the UI via the {@link UIAccessor} before being applied. A {@link UITask} can
 * be used for long running processes that may need to synchronize with the UI.
 * </p>
 * <p>
 * The most common use case for this interface is on a controller or view-model
 * that can be enabled for asynchronous changes via a call from a UI attach
 * method and then disabled via a UI detach method. The view-model/controller
 * doesn't need to be aware of the UI details but can simply begin asynchronous
 * operations (such as using a thread pool or subscribing to a data feed) and
 * update the UI using the provided {@link UIAccessor}. It is important to
 * always call disable after calling enable otherwise there could be resource
 * leaks such as stale listeners or orphaned threads.
 * </p>
 *
 * @author mpilone
 * @see UITask
 */
public interface AsynchronousDataChangeAware {

  /**
   * Enables handling of background, asynchronous data changes. Data changes
   * that could affect the UI must be synchronized using the given
   * {@link UIAccessor}.
   *
   * @param uiAccessor the accessor that can synchronize and access the UI
   * safely from a background thread
   */
  void enableAsynchronousDataChanges(UIAccessor uiAccessor);

  /**
   * Disables handling of background, asynchronous data changes. This method
   * should always be called if an enable call was made on the same
   * implementation. It is recommended that implementations handle and ignore
   * calls to this method if not enabled.
   */
  void disableAsynchronousDataChanges();
}
