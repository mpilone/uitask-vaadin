
package org.mpilone.vaadin.uitask;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.easymock.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.vaadin.ui.UI;

/**
 * Test case for the {@link UITask} class.
 *
 * @author mpilone
 */
@RunWith(EasyMockRunner.class)
public class UITaskTestCase extends EasyMockSupport {

  @Mock
  private UI ui;

  @Mock
  private Future<Void> accessFuture;

  /**
   * Tests running the UITask in a simple success case.
   *
   * @throws InterruptedException
   * @throws ExecutionException
   */
  @Test
  public void testRun() throws InterruptedException, ExecutionException {

    // Given
    final String testResult = "foo";
    final UITask task = partialMockBuilder(UITask.class).withConstructor(
        new UIAccessor.Fixed(ui)).createMock();

    expect(task.runInBackground()).andReturn(testResult);
    expect(ui.access(isA(Runnable.class))).andAnswer(() -> {
      Runnable r = (Runnable) EasyMock.getCurrentArguments()[0];
      r.run();
      return accessFuture;
    });
    task.done();

    // When
    replayAll();

    task.run();

    // Then
    verifyAll();

    assertFalse(task.isCancelled());
    assertEquals(testResult, task.get());
  }

  /**
   * Tests running the UITask that gets cancelled.
   *
   * @throws InterruptedException
   * @throws ExecutionException
   */
  @Test(expected = CancellationException.class)
  public void testRun_Cancelled() throws InterruptedException,
      ExecutionException {

    // Given
    final CancelSelfTask task = new CancelSelfTask(new UIAccessor.Fixed(ui));

    expect(ui.access(isA(Runnable.class))).andAnswer(() -> {
      Runnable r = (Runnable) EasyMock.getCurrentArguments()[0];
      r.run();
      return accessFuture;
    });

    // When
    replayAll();

    task.run();

    // Then
    verifyAll();

    assertTrue(task.isCancelled());
    assertTrue(task.doneCalled);
    assertTrue(task.cancelledInDone);

    // Exception expected.
    task.get();
  }

  /**
   * Test task that cancels itself and records if the cancel occurred.
   */
  private class CancelSelfTask extends UITask<String> {

    private final static String RESULT = "Bar";
    private boolean doneCalled;
    private boolean cancelledInDone;

    public CancelSelfTask(UIAccessor uiAccessor) {
      super(uiAccessor);
    }

    @Override
    protected String runInBackground() {
      cancel(false);

      return RESULT;
    }

    @Override
    protected void done() {
      doneCalled = true;
      cancelledInDone = isCancelled();
    }
  }

}
