package mgh14.search.live.service;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import mgh14.search.live.model.wallpaper.ExpiredResourcesDeleter;
import mgh14.search.live.model.wallpaper.WindowsWallpaperSetter;
import mgh14.search.live.model.web.util.ImageUtils;
import mgh14.search.live.model.wallpaper.QueueLoader;
import mgh14.search.live.model.web.resource.getter.ResourceUrlGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that cycles desktop wallpaper resources
 */
@Component
public class ResourceCycler {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  private static final int DEFAULT_SECONDS_TO_SLEEP = 300;
  private static final int SECONDS_TO_TIMEOUT = 30;
  private static final String DIRECTORY_TIME_APPENDER = "-time";

  @Autowired
  private ExecutorService executorService;
  @Autowired
  private ResourceUrlGetter resourceUrlGetter;
  @Autowired
  private ConcurrentLinkedQueue<String> resourcesQueue;
  @Autowired
  private QueueLoader queueLoader;
  @Autowired
  private WindowsWallpaperSetter setter;
  @Autowired
  private ExpiredResourcesDeleter deleter;
  @Autowired
  private ImageUtils imageUtils;

  private List<String> filenames = new LinkedList<String>();
  private String absoluteCurrentFilename;
  private String searchStringFolder;
  private AtomicInteger secondsToSleep;

  private AtomicBoolean isCycleActive;
  private AtomicBoolean getNextResource;

  public ResourceCycler() {
    absoluteCurrentFilename = null;
    searchStringFolder = null;

    secondsToSleep = new AtomicInteger();
    setSecondsToSleep(DEFAULT_SECONDS_TO_SLEEP);

    isCycleActive = new AtomicBoolean();
    setCycleActive(true);
    getNextResource = new AtomicBoolean();
    setGetNextResource(false);
  }

  public void setSecondsToSleep(int secondsToSleep) {
    this.secondsToSleep.set(secondsToSleep);
  }

  public void startCycle(final String searchString) {
    if (searchString == null || searchString.isEmpty()) {
      Log.error("Please enter a search query (e.g. \"desktop wallpaper\"");
      return;
    }
    resourceUrlGetter.setSearchString(searchString);
    searchStringFolder = searchString.replace(" ", "-") + DIRECTORY_TIME_APPENDER
      + System.currentTimeMillis() + "\\";

    // run resource cycle
    runCycle();
  }

  private void runCycle() {
    Log.debug("Starting wallpaper cycle...");
    queueLoader.startResourceDownloads();

    final long secondsToSleepInMillis = secondsToSleep.get() * 1000;
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        while (true) {
          while (resourcesQueue.isEmpty()) {
            if (!queueLoader.isDownloading()) {
              queueLoader.startResourceDownloads();
            }
          }

          if (isCycleActive.get()) {
            // check that filename from queue is valid
            String filename = resourcesQueue.poll();
            if (filename == null) {
              continue;
            }

            // Set image to desktop and sleep
            if (imageUtils.canOpenImage(filename)) {
              filenames.add(filename);
              absoluteCurrentFilename = filename;

              // set image to desktop
              setter.setDesktopWallpaper(filename);

              // sleep for x milliseconds (enjoy the background!)
              sleep(System.currentTimeMillis(), secondsToSleepInMillis);
            }
            else {
              Log.error("Couldn't open file: [{}]. " +
                "Deleting and moving to next resource...", filename);
              deleter.deleteFile(new File(filename).toPath());
            }
          }
        }
      }
    });
  }

  public String saveCurrentImage() {
    return imageUtils.saveImage(searchStringFolder, absoluteCurrentFilename);
  }

  public void pauseCycle() {
    Log.debug("Pausing resource cycle...");
    setCycleActive(false);
  }

  public void resumeCycle() {
    Log.debug("Resuming resource cycle...");
    setCycleActive(true);
  }

  public void getNextResource() {
    Log.debug("Getting next resource...");
    setGetNextResource(true);
  }

  public void deleteAllResources() {
    Log.debug("Deleting all resources...");
    deleter.deleteAllResources();
  }

  private void setCycleActive(boolean cycleActive) {
    isCycleActive.set(cycleActive);
  }

  private void setGetNextResource(boolean newGetNextResource) {
    getNextResource.set(newGetNextResource);
  }

  private void sleep(final long sleepStartTime, long secondsToSleepInMillis) {
    while (!getNextResource.get() &&
      (System.currentTimeMillis() - sleepStartTime) <
        secondsToSleepInMillis) {
    }
    if (getNextResource.get()) {
      Log.debug("Skipping to next resource...");
      setGetNextResource(false);
    }
  }

}
