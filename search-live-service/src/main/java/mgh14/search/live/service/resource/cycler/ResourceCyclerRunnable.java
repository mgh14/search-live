package mgh14.search.live.service.resource.cycler;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import mgh14.search.live.model.web.util.FileUtils;
import mgh14.search.live.model.wallpaper.QueueLoader;
import mgh14.search.live.model.wallpaper.WindowsWallpaperSetter;
import mgh14.search.live.model.web.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that cycles desktop wallpaper resources
 */
@Component
class ResourceCyclerRunnable implements Runnable {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());
  private static final int DEFAULT_SECONDS_TO_SLEEP = 300;

  @Autowired
  private QueueLoader queueLoader;
  @Autowired
  private ConcurrentLinkedQueue<String> resourcesQueue;
  @Autowired
  private WindowsWallpaperSetter setter;
  @Autowired
  private ImageUtils imageUtils;
  @Autowired
  private FileUtils fileUtils;

  private List<String> filenames = new LinkedList<String>();
  private AtomicBoolean isCycleActive;
  private AtomicInteger secondsToSleep;
  private AtomicReference<String> currentAbsoluteFilename;
  private AtomicBoolean getNextResource;

  ResourceCyclerRunnable() {
    currentAbsoluteFilename = new AtomicReference<String>(null);
    isCycleActive = new AtomicBoolean(true);
    secondsToSleep = new AtomicInteger(DEFAULT_SECONDS_TO_SLEEP);
    getNextResource = new AtomicBoolean(false);
  }

  @Override
  public void run() {
    final long secondsToSleepInMillis = secondsToSleep.get() * 1000;
    queueLoader.startResourceDownloads();

    while (true) {
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

  String saveCurrentImage(String searchStringFolder) {
    return imageUtils.saveImage(searchStringFolder,
      currentAbsoluteFilename.get());
  }

  private void sleep(final long sleepStartTime, long secondsToSleepInMillis) {
    while (!getNextResource.get() &&
      (System.currentTimeMillis() - sleepStartTime) <
        secondsToSleepInMillis) {
    }
    if (getNextResource.get()) {
      Log.debug("Skipping to next resource...");
      getNextResource.set(false);
    }
  }

}

