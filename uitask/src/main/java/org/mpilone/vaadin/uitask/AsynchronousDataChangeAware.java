
package org.mpilone.vaadin.uitask;

/**
 * A component that can listen for data changes that happen asynchronously,
 * outside of the normal UI lock. These data changes must be synchronized with
 * the UI via the {@link UIAccessor} before being applied. A {@link UITask} can
 * be used for long running processes that may need to synchronize with the UI.
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
   * Disables handling of background, asynchronous data changes.
   */
  void disableAsynchronousDataChanges();
}
