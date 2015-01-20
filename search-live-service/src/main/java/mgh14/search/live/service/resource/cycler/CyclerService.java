package mgh14.search.live.service.resource.cycler;

import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;

import mgh14.search.live.model.wallpaper.QueueLoader;
import mgh14.search.live.model.wallpaper.WindowsWallpaperSetter;
import mgh14.search.live.model.web.resource.getter.ResourceUrlGetter;
import mgh14.search.live.model.web.util.FileUtils;
import mgh14.search.live.model.web.util.ImageUtils;
import mgh14.search.live.service.messaging.CycleAction;
import mgh14.search.live.model.observable.messaging.ObserverMessageBuilder;
import mgh14.search.live.model.observable.messaging.ObserverMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Service responsible for running the runnable resource cycler
 * code. This class serves as the service layer between the
 * command executor and the remainder of the classes.
 */
@Component
public class CyclerService extends Observable implements Observer {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());
  private static final String DIRECTORY_TIME_APPENDER = "-time";

  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private RetryTimerRunnable retryTimerRunnable;
  @Autowired
  private ExecutorService executorService;
  @Autowired
  private ResourceUrlGetter resourceUrlGetter;
  @Autowired
  private QueueLoader queueLoader;
  @Autowired
  private FileUtils fileUtils;
  @Autowired
  private ObserverMessageBuilder observerMessageBuilder;

  private String searchStringFolder;
  private int secondsToSleep;
  private ResourceCyclerRunnable resourceCyclerRunnable;

  public CyclerService() {
    searchStringFolder = null;
    resourceCyclerRunnable = null;
  }

  @PostConstruct
  public void addInternalObservedObjects() {
    fileUtils.addObserver(this);
  }

  public void setSecondsToSleep(int secondsToSleep) {
    this.secondsToSleep = secondsToSleep;
  }

  public void startService(final String searchString) {
    if (searchString == null || searchString.isEmpty()) {
      Log.error("Please enter a search query (e.g. \"desktop wallpaper\"");
      return;
    }

    // reset runnables and queue loader
    if (resourceCyclerRunnable != null) {
      resourceCyclerRunnable.interruptRunnable();
      resourceCyclerRunnable.deleteObserver(this);
    }
    retryTimerRunnable.interruptRunnable();
    queueLoader.resetQueueLoader();

    // set properties for new search
    resourceCyclerRunnable = getNewResourceCyclerRunnable();
    resourceUrlGetter.setSearchString(searchString);
    searchStringFolder = searchString.replace(" ", "-") + DIRECTORY_TIME_APPENDER
      + System.currentTimeMillis() + "\\";

    // run resource cycle
    Log.debug("Starting wallpaper cycle...");
    queueLoader.startResourceDownloads();
    runRetryTimer();
    // instead of submitting with the ability to stop the thread,
    // we provide finer-grained control with the threadInterrupted
    // variable in the ResourceCyclerRunnable class and thus have
    // no need for the Future object.
    executorService.execute(resourceCyclerRunnable);
  }

  public String saveCurrentImage() {
    final String savedImageFilename = resourceCyclerRunnable
      .saveCurrentImage(searchStringFolder);

    // build observer message
    String successOrFailure = (savedImageFilename != null) ?
      ObserverMessageProcessor.MESSAGE_SUCCESS :
      ObserverMessageProcessor.MESSAGE_FAILURE;
    String resourceIdentifier = (savedImageFilename != null) ?
      savedImageFilename :
      resourceCyclerRunnable.getCurrentFilename();
    final String observerMessage = observerMessageBuilder
      .buildObserverMessage(CycleAction.SAVE.name(),
        successOrFailure, resourceIdentifier);
    notifyObserversWithMessage(observerMessage);

    return savedImageFilename;
  }

  public void pauseCycle() {
    Log.debug("Pausing resource cycle...");
    resourceCyclerRunnable.setIsCycleActive(false);
    notifyObserversWithMessage(observerMessageBuilder
      .buildObserverMessage(CycleAction.PAUSE.name(),
        ObserverMessageProcessor.MESSAGE_SUCCESS));
  }

  public void resumeCycle() {
    Log.debug("Resuming resource cycle...");
    resourceCyclerRunnable.setIsCycleActive(true);
    notifyObserversWithMessage(observerMessageBuilder
      .buildObserverMessage(CycleAction.RESUME.name(),
        ObserverMessageProcessor.MESSAGE_SUCCESS));
  }

  public void getNextResource() {
    Log.debug("Getting next resource...");
    resourceCyclerRunnable.setGetNextResource(true);
  }

  public void deleteAllResources() {
    Log.debug("Deleting all resources...");
    fileUtils.deleteAllFiles(new File(
      fileUtils.getResourceDir()));
  }

  private void runRetryTimer() {
    Log.debug("Starting retry timer...");
    executorService.execute(retryTimerRunnable);
  }

  private void notifyObserversWithMessage(String message) {
    setChanged();
    notifyObservers(message);
  }

  private ResourceCyclerRunnable getNewResourceCyclerRunnable() {
    ResourceCyclerRunnable resourceCyclerRunnable =
      new ResourceCyclerRunnable(
        applicationContext.getBean(QueueLoader.class),
        (ConcurrentLinkedQueue<String>)
          applicationContext.getBean("resourceQueue"),
        applicationContext.getBean(WindowsWallpaperSetter.class),
        applicationContext.getBean(ImageUtils.class),
        applicationContext.getBean(FileUtils.class));
    resourceCyclerRunnable.setIsCycleActive(true);
    resourceCyclerRunnable.setSecondsToSleep(secondsToSleep);

    resourceCyclerRunnable.addObserver(this);

    return resourceCyclerRunnable;
  }

  @Override
  public void update(Observable o, Object arg) {
    final String message = (String) arg;

    // from observing the resource cycler runnable
    if (o instanceof ResourceCyclerRunnable) {
      processResourceCyclerRunnableMessage(message);
    }

    // from observing the file utils class
    if (o instanceof FileUtils) {
      processFileUtilsMessage(message);
    }
  }

  private void processResourceCyclerRunnableMessage(String message) {
    if (ResourceCyclerRunnable.RESOURCE_CYCLE_STARTED_MESSAGE
      .equals(message)) {

      notifyObserversWithMessage((observerMessageBuilder
        .buildObserverMessage(CycleAction.START_SERVICE.name(),
          ObserverMessageProcessor.MESSAGE_SUCCESS)));
    }
    else if (ResourceCyclerRunnable.RESOURCE_CYCLE_START_FAILED_MESSAGE
      .equals(message)) {

      notifyObserversWithMessage((observerMessageBuilder
        .buildObserverMessage(CycleAction.START_SERVICE.name(),
          ObserverMessageProcessor.MESSAGE_FAILURE)));
    }
    else if(ResourceCyclerRunnable.RESOURCE_SKIPPED_MESSAGE_SUCCESS
      .equals(message)) {

      notifyObserversWithMessage((observerMessageBuilder
        .buildObserverMessage(CycleAction.NEXT.name(),
          ObserverMessageProcessor.MESSAGE_SUCCESS)));
    }
    else if(ResourceCyclerRunnable.RESOURCE_SKIPPED_MESSAGE_FAILURE
      .equals(message)) {

      notifyObserversWithMessage((observerMessageBuilder
        .buildObserverMessage(CycleAction.NEXT.name(),
          ObserverMessageProcessor.MESSAGE_FAILURE)));
    }
  }

  private void processFileUtilsMessage(String message) {
    if (FileUtils.RESOURCES_SUCCESSFULLY_DELETED_MESSAGE.equals(message)) {
      notifyObserversWithMessage(observerMessageBuilder
        .buildObserverMessage(CycleAction.DELETE_RESOURCES.name(),
          ObserverMessageProcessor.MESSAGE_SUCCESS));
    }
    else if (FileUtils.RESOURCES_FAILED_TO_DELETE_MESSAGE.equals(message)) {
      notifyObserversWithMessage(observerMessageBuilder
        .buildObserverMessage(CycleAction.DELETE_RESOURCES.name(),
          ObserverMessageProcessor.MESSAGE_FAILURE));
    }
  }

}
