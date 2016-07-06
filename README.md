# UITask for Vaadin Add-on

UITask is a server-side add-on for Vaadin 7 that provides a simple framework for executing background tasks and updating the UI safely.

The add-on is primarily composed of a task that can be run on a background thread via an [Executor](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html) and then complete the work safely in the UI thread/lock via a UIAccessor. Vaadin is not thread-safe, therefore all UI modifications must be done after obtaining the UI lock. This add-on attempts to simplify that process by providing an implementation of [Future](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html) and exposing methods that are safely called in the UI thread.

The add-on was inspired by by Swing's [SwingWorker](https://docs.oracle.com/javase/8/docs/api/javax/swing/SwingWorker.html) and JavaFX's [Task](https://docs.oracle.com/javafx/2/api/javafx/concurrent/Task.html).

## Benefits

- Works with any standard Java Executor implementation.
- Uses the standard Java Runnable and Future interfaces to avoid complexity.
- Provides simple interfaces to separate UI code from backend code and to support dependency injection/mocking.
- Provides access to the UI for manual synchronization such as progress updates.

## Example Usage

### UIAccessor

UIAccessor is a simple interface that exposes the `access()` and `accessSynchronously()` methods of [UI](https://vaadin.com/api/7.6.6/com/vaadin/ui/UI.html). The interface provides a separation of GUI code/dependencies from backend code which may help when using an MVVM or MVC pattern as well as assist with mocking in unit tests.

In most cases you can implement the interface in your UI subclass with no additional code. However you can also use the nested implementations UIAccessor.Fixed and UIAccessor.Current depending on the scenario. For example:

```
public class MyAppUI extends UI implements UIAccessor {
  // ...
}
```

### UITask

UITask is the heart of the add-on. The task implements [RunnableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/RunnableFuture.html) allowing it to be submitted to a standard Java Executor and then monitored or cancelled via the Future interface. Subclasses of the task must implement the `runInBackground()` method to perform any background work. When the background work is complete, the `done()` method is automatically called in the UI lock. The implementation can then use any of the Future methods to perform additional logic such as getting the result or checking for the cancelled state. For example:

```
class MyTask extends UITask<Integer> {

  protected Integer runInBackground() {
    // Some long running calculation that doesn't use the UI.
    return 42;
  }

  protected done() {
    // Check if the task was cancelled during execution.
    if (!isCancelled()) {
      int result = get();

      // Apply the UI change. Either directly modify components 
      // or update an item/container for bound data.
      someUiComponent.setValue(result);  
    }
  }
}

Future<Integer> task = new MyTask(new UIAccessor.Current());
executor.execute(task);
```
#### Canceling a Task

UITask implements the Future interface so you can cancel the task at any time. Canceling only prevents the background work from executing but it does not prevent the `done()` method from being called so cleanup work can always be performed. The `done()` method should check if the task was cancelled before calling `get()` and may want to apply different changes to the UI depending on the canceled state.

```
Future<Integer> task = new MyTask(new UIAccessor.Fixed(ui));
executor.execute(task);

// User click's cancel in the UI.
task.cancel(false);
```

### ProgressUITask

ProgressUITask is provided which exposes observable properties that can be used for simple cases where the progress of the background task needs to be monitored. An implementation can extend ProgressUITask, implement the `runInBackground()` method, and use the `updateTotal()` and `updateProgress()` methods. For example:

```
class MyTask extends ProgressUITask<File> {

  protected Integer runInBackground() {
    // Some long running calculation that doesn't use the UI.
    updateTotal(100);

    for (int i = 0; i < 100; i++) {
      // Do some work.
      updateProgress(i);
      updateMessage(format("Downloading segment %d of 100", i));
    }

    return new File("/var/tmp/download.tmp");
  }

  protected done() {
    if (!isCancelled()) {
      File result = get();

      someUiComponent.setValue("Download complete: " + result.getAbsolutePath());
    }
    else {
      someUiComponent.setValue("Download cancelled.");
    }
  }
}

ProgressUITask<File> task = new MyTask(injectedUiAccessor);

// Bind the properties directly to the UI components.
totalLabel.setPropertyDataSource(task.getTotal());
progressLabel.setPropertyDataSource(task.getProgress());;
messageLabel.setPropertyDataSource(task.getMessage());

// Execute the task.
executor.execute(task);
```

## Download release

Official releases of this add-on are available at Vaadin Directory. For Maven instructions, download and reviews, go to http://vaadin.com/addon/uitask-vaadin

## Release notes

### Version 1.1.0
- Added test case for canceling before run.
- Updated documentation.

### Version 1.0.0
- Initial release
- UITask and UIAccessor implementation.
- ProgressUITask example usage implementation.

## Roadmap

This component is developed as a hobby with no public roadmap or any guarantees of upcoming releases.

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. 

## Contributions

Contributions are welcome, but there are no guarantees that they are accepted as such. Process for contributing is the following:
- Fork this project
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- Refer to the fixed issue in commit
- Send a pull request for the original project
- Comment on the original issue that you have implemented a fix for it

## License & Author

Add-on is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

UITask for Vaadin is written by [Mike Pilone](https://github.com/mpilone) with support from the [Public Radio Satellite System (PRSS)](http://www.prss.org).
