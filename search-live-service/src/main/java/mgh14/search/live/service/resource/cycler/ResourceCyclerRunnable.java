package mgh14.search.live.service.resource.cycler;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import mgh14.search.live.model.observable.messaging.ObserverMessageBuilder;
import mgh14.search.live.model.observable.messaging.ObserverMessageProcessor;
import mgh14.search.live.model.wallpaper.QueueLoader;
import mgh14.search.live.model.wallpaper.WindowsWallpaperSetter;
import mgh14.search.live.model.web.util.FileUtils;
import mgh14.search.live.model.web.util.ImageUtils;
import mgh14.search.live.service.messaging.CycleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that cycles desktop wallpaper resources
 */
public class ResourceCyclerRunnable extends Observable
  implements Runnable  {

  private final Logger Log = LoggerFactory.getLogger(
    getClass().getSimpleName());
  private static final int DEFAULT_SECONDS_TO_SLEEP = 300;

  private QueueLoader queueLoader;
  private ConcurrentLinkedQueue<String> resourcesQueue;
  private WindowsWallpaperSetter setter;
  private ImageUtils imageUtils;
  private FileUtils fileUtils;
  private ObserverMessageBuilder observerMessageBuilder;

  private List<String> filenames = new LinkedList<String>();
  private AtomicBoolean isCycleActive;
  private AtomicInteger secondsToSleep;
  private AtomicReference<String> currentAbsoluteFilename;
  private AtomicBoolean isSleeping;
  private AtomicBoolean getNextResource;
  private AtomicBoolean threadInterrupted;

  ResourceCyclerRunnable(QueueLoader queueLoader,
      ConcurrentLinkedQueue<String> resourcesQueue,
      WindowsWallpaperSetter setter, ImageUtils imageUtils,
      FileUtils fileUtils,
      ObserverMessageBuilder observerMessageBuilder) {

    this.queueLoader = queueLoader;
    this.resourcesQueue = resourcesQueue;
    this.setter = setter;
    this.imageUtils = imageUtils;
    this.fileUtils = fileUtils;
    this.observerMessageBuilder = observerMessageBuilder;

    currentAbsoluteFilename = new AtomicReference<String>(null);
    isCycleActive = new AtomicBoolean(true);
    secondsToSleep = new AtomicInteger(DEFAULT_SECONDS_TO_SLEEP);
    isSleeping = new AtomicBoolean(false);
    getNextResource = new AtomicBoolean(false);
    threadInterrupted = new AtomicBoolean(false);
  }

  String getCurrentFilename() {
    return (currentAbsoluteFilename != null) ?
      currentAbsoluteFilename.get() : null;
  }

  void setIsCycleActive(boolean isCycleActive,
    boolean isAPauseOrResume) {
    this.isCycleActive.set(isCycleActive);

    if (isAPauseOrResume) {
      final String action = (!isCycleActive) ?
        CycleAction.PAUSE.name() : CycleAction.RESUME.name();
      notifyObserversWithMessage(observerMessageBuilder
        .buildObserverMessage(action,
          ObserverMessageProcessor.MESSAGE_SUCCESS));
    }
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
    notifyObserversWithMessage(observerMessageBuilder
      .buildObserverMessage(CycleAction.START_SERVICE.name(),
        ObserverMessageProcessor.MESSAGE_SUCCESS));

    final long secondsToSleepInMillis =
      secondsToSleep.get() * 1000;
    queueLoader.startResourceDownloads();
    threadInterrupted.set(false);

    long sleepStartTime = 0;
    while (!threadInterrupted.get()) {
      if (!isCycleActive.get()) {
        continue;
      }

      if (isSleeping.get() && !getNextResource.get()) {
        if (System.currentTimeMillis() - sleepStartTime >
          secondsToSleepInMillis) {

          Log.debug("Sleep for resource {} has finished. Moving to " +
            "next resource...", getCurrentFilename());
          sleepStartTime = 0;
          isSleeping.set(false);
          // TODO: Add "getting next resource" notification
        }
        else {
          continue;
        }
      }
      else if (isSleeping.get() && getNextResource.get()) {
        isSleeping.set(false);
        sleepStartTime = 0;
        getNextResource.set(false);
        Log.debug("Skipping from {} to next resource...",
          getCurrentFilename());
        notifyObserversWithMessage(observerMessageBuilder
          .buildObserverMessage(CycleAction.NEXT.name(),
            ObserverMessageProcessor.MESSAGE_SUCCESS,
            fileUtils.getResourceFilenameFromPath(
              getCurrentFilename())));
      }

      if (!resourcesQueue.isEmpty()) {
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
          sleepStartTime = System.currentTimeMillis();
          isSleeping.set(true);
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

  private void notifyObserversWithMessage(String message) {
    setChanged();
    notifyObservers(message);
  }

}

