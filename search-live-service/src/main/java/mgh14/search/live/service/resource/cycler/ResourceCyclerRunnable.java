package mgh14.search.live.service.resource.cycler;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import mgh14.search.live.model.wallpaper.QueueLoader;
import mgh14.search.live.model.wallpaper.WindowsWallpaperSetter;
import mgh14.search.live.model.web.util.FileUtils;
import mgh14.search.live.model.web.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that cycles desktop wallpaper resources
 */
class ResourceCyclerRunnable extends Observable implements Runnable  {

  public static final String RESOURCE_CYCLE_STARTED_MESSAGE =
    "Resource cycle started.";
  public static final String RESOURCE_CYCLE_START_FAILED_MESSAGE =
    "";
  public static final String RESOURCE_SKIPPED_MESSAGE_SUCCESS =
    "Resource skipped.";
  public static final String RESOURCE_SKIPPED_MESSAGE_FAILURE =
    "";

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());
  private static final int DEFAULT_SECONDS_TO_SLEEP = 300;

  private QueueLoader queueLoader;
  private ConcurrentLinkedQueue<String> resourcesQueue;
  private WindowsWallpaperSetter setter;
  private ImageUtils imageUtils;
  private FileUtils fileUtils;

  private List<String> filenames = new LinkedList<String>();
  private AtomicBoolean isCycleActive;
  private AtomicInteger secondsToSleep;
  private AtomicReference<String> currentAbsoluteFilename;
  private AtomicBoolean getNextResource;
  private AtomicBoolean threadInterrupted;

  ResourceCyclerRunnable(QueueLoader queueLoader,
      ConcurrentLinkedQueue<String> resourcesQueue,
      WindowsWallpaperSetter setter, ImageUtils imageUtils,
      FileUtils fileUtils) {

    this.queueLoader = queueLoader;
    this.resourcesQueue = resourcesQueue;
    this.setter = setter;
    this.imageUtils = imageUtils;
    this.fileUtils = fileUtils;

    currentAbsoluteFilename = new AtomicReference<String>(null);
    isCycleActive = new AtomicBoolean(true);
    secondsToSleep = new AtomicInteger(DEFAULT_SECONDS_TO_SLEEP);
    getNextResource = new AtomicBoolean(false);
    threadInterrupted = new AtomicBoolean(false);
  }

  String getCurrentFilename() {
    return (currentAbsoluteFilename != null) ? currentAbsoluteFilename.get()
      : null;
  }

  void setIsCycleActive(boolean isCycleActive) {
    this.isCycleActive.set(isCycleActive);
  }

  void setSecondsToSleep(int secondsToSleep) {
    this.secondsToSleep.set(secondsToSleep);
  }

  void setGetNextResource(boolean newGetNextResource) {
    getNextResource.set(newGetNextResource);
  }

  void interruptRunnable() {
    threadInterrupted.set(true);
  }

  @Override
  public void run() {
    notifyObserversWithMessage(RESOURCE_CYCLE_STARTED_MESSAGE);

    final long secondsToSleepInMillis = secondsToSleep.get() * 1000;
    queueLoader.startResourceDownloads();
    threadInterrupted.set(false);

    while (!threadInterrupted.get()) {
      if (isCycleActive.get() && !resourcesQueue.isEmpty()) {
        // check that filename from queue is valid
        String filename = resourcesQueue.poll();
        if (filename == null) {
          continue;
        }

        // Set image to desktop and sleep
        if (imageUtils.canOpenImage(filename)) {
          filenames.add(filename);
          currentAbsoluteFilename.set(filename);

          // set image to desktop
          setter.setDesktopWallpaper(filename);

          // sleep for x milliseconds (enjoy the background!)
          sleep(System.currentTimeMillis(), secondsToSleepInMillis);
        }
        else {
          Log.error("Couldn't open file: [{}]. " +
            "Deleting and moving to next resource...", filename);
          fileUtils.deleteFile(new File(filename).toPath());
        }
      }
    }

    Log.debug("Thread interrupted: {}", Thread.currentThread());
  }

  String saveCurrentImage(String searchStringFolder) {
    return imageUtils.saveImage(searchStringFolder,
      currentAbsoluteFilename.get());
  }

  private void sleep(final long sleepStartTime, long secondsToSleepInMillis) {
    while (!getNextResource.get() && isCycleActive.get() &&
      (System.currentTimeMillis() - sleepStartTime) <
        secondsToSleepInMillis) {
    }
    if (getNextResource.get()) {
      Log.debug("Skipping to next resource...");
      getNextResource.set(false);
      notifyObserversWithMessage(RESOURCE_SKIPPED_MESSAGE_SUCCESS);
    }
  }

  private void notifyObserversWithMessage(String message) {
    setChanged();
    notifyObservers(message);
  }

}

