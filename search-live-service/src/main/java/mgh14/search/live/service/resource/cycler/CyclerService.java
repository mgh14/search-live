package mgh14.search.live.service.resource.cycler;

import java.io.File;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.prefs.Preferences;
import javax.annotation.PostConstruct;

import mgh14.search.live.model.ParamNames;
import mgh14.search.live.model.notification.NotificationProcessor;
import mgh14.search.live.model.observable.messaging.ObserverMessageBuilder;
import mgh14.search.live.model.observable.messaging.ObserverMessageProcessor;
import mgh14.search.live.model.wallpaper.QueueLoader;
import mgh14.search.live.model.wallpaper.WindowsWallpaperSetter;
import mgh14.search.live.model.web.resource.getter.ResourceUrlGetter;
import mgh14.search.live.model.web.util.FileUtils;
import mgh14.search.live.model.web.util.ImageUtils;
import mgh14.search.live.service.messaging.CycleAction;
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
public class CyclerService extends Observable {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());
  private static final String DIRECTORY_TIME_APPENDER = "-time";

  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private Preferences preferences;
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
  @Autowired
  private NotificationProcessor cyclerServiceProcessor;

  private String searchStringFolder;
  private int secondsToSleep;
  private ResourceCyclerRunnable resourceCyclerRunnable;

  public CyclerService() {
    searchStringFolder = null;
    resourceCyclerRunnable = null;
  }

  @PostConstruct
  public void addInternalObservedObjects() {
    fileUtils.addObserver(cyclerServiceProcessor);
  }

  public void setResourceSaveDir(String newResourceDirFilepath) {
    if (newResourceDirFilepath != null &&
      !newResourceDirFilepath.isEmpty()) {
      Log.debug("Setting preferences resource save dir: [{}]",
        newResourceDirFilepath);
      preferences.put(ParamNames.RESOURCE_SAVE_DIR,
        newResourceDirFilepath);
    }
    else {
      Log.warn("Null/empty directory for saving resources has " +
        "been passed as a param. (Was it not chosen by the user?)");

      final String saveDirPref = preferences.get(
        ParamNames.RESOURCE_SAVE_DIR, "");
      if (saveDirPref == null || saveDirPref.isEmpty()) {
        Log.error("No save dir preference exists. " +
          "Save function will not be available.");
      }
    }
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
      resourceCyclerRunnable.deleteObserver(
        cyclerServiceProcessor);
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
    final String savedImageFilename = fileUtils
      .getResourceFilenameFromPath(resourceCyclerRunnable
        .saveCurrentImage(searchStringFolder));

    // build observer message
    String successOrFailure = (savedImageFilename != null) ?
      ObserverMessageProcessor.MESSAGE_SUCCESS :
      ObserverMessageProcessor.MESSAGE_FAILURE;
    final String observerMessage = observerMessageBuilder
      .buildObserverMessage(CycleAction.SAVE.name(),
        successOrFailure, savedImageFilename);
    notifyObserversWithMessage(observerMessage);

    return savedImageFilename;
  }

  public void pauseCycle() {
    Log.debug("Pausing resource cycle...");
    resourceCyclerRunnable.setIsCycleActive(false, true);
  }

  public void resumeCycle() {
    Log.debug("Resuming resource cycle...");
    resourceCyclerRunnable.setIsCycleActive(true, true);
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
    final ResourceCyclerRunnable resourceCyclerRunnable =
      new ResourceCyclerRunnable(
        applicationContext.getBean(QueueLoader.class),
        (ConcurrentLinkedQueue<String>)
          applicationContext.getBean("resourceQueue"),
        applicationContext.getBean(WindowsWallpaperSetter.class),
        applicationContext.getBean(ImageUtils.class),
        applicationContext.getBean(FileUtils.class),
        observerMessageBuilder);
    resourceCyclerRunnable.setIsCycleActive(true, false);
    resourceCyclerRunnable.setSecondsToSleep(secondsToSleep);

    resourceCyclerRunnable.addObserver(cyclerServiceProcessor);

    return resourceCyclerRunnable;
  }

}
