package mgh14.search.live.service;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import mgh14.search.live.model.wallpaper.WallpaperDeleter;
import mgh14.search.live.model.wallpaper.WindowsWallpaperSetter;
import mgh14.search.live.model.web.ImageUtils;
import mgh14.search.live.model.web.QueueLoader;
import mgh14.search.live.model.web.ResourceUrlGetter;
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
  private WallpaperDeleter deleter;
  @Autowired
  private ImageUtils imageUtils;

  private List<String> filenames = new LinkedList<String>();
  private String absoluteCurrentFilename;
  private String searchStringFolder;
  private int secondsToSleep;

  // TODO: this field is for debugging. Will be removed later
  private int numResultsToRetrieve;

  private AtomicBoolean isCycleActive;
  private AtomicBoolean getNextResource;

  public ResourceCycler() {
    absoluteCurrentFilename = null;
    searchStringFolder = null;

    setSecondsToSleep(DEFAULT_SECONDS_TO_SLEEP);

    isCycleActive = new AtomicBoolean();
    setCycleActive(true);
    getNextResource = new AtomicBoolean();
    setGetNextResource(false);
  }

  public void setNumResultsToRetrieve(int numResultsToRetrieve) {
    this.numResultsToRetrieve = numResultsToRetrieve;
  }

  public void setSecondsToSleep(int secondsToSleep) {
    this.secondsToSleep = secondsToSleep;
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

    executorService.execute(new Runnable() {
      @Override
      public void run() {
        while (true) {
          while (resourcesQueue.isEmpty()) {}

          if (isCycleActive.get()) {
            // check that filename from queue is valid
            String filename = resourcesQueue.poll();
            if (filename == null) {
              continue;
            }

            // Check if more resources need to be downloaded
            final int resourceNum = getResourceNumFromFilename(filename);
            if (resourceNum % (numResultsToRetrieve - 1) == 0) {
              queueLoader.startResourceDownloads();
            }

            // Set image to desktop and sleep
            if (imageUtils.canOpenImage(filename)) {
              filenames.add(filename);
              absoluteCurrentFilename = filename;

              // set image to desktop
              setter.setDesktopWallpaper(filename);

              // sleep for x milliseconds (enjoy the background!)
              final long secondsToSleepInMillis = secondsToSleep * 1000;
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

  public String saveCurrentImage() {
    return imageUtils.saveImage(searchStringFolder, absoluteCurrentFilename);
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

  private int getResourceNumFromFilename(String filename) {
    if (filename == null || filename.isEmpty()) {
      return -1;
    }

    return Integer.parseInt(filename.substring(
      filename.indexOf(QueueLoader.RESOURCE_FILENAME_PREPEND) +
        QueueLoader.RESOURCE_FILENAME_PREPEND.length(),
      filename.lastIndexOf(QueueLoader.RESOURCE_FILENAME_TIMESTAMP_SEPARATOR)));
  }

}
