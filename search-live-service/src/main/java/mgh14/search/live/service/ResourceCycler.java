package mgh14.search.live.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;

import mgh14.search.live.model.wallpaper.WallpaperDeleter;
import mgh14.search.live.model.wallpaper.WindowsWallpaperSetter;
import mgh14.search.live.model.web.QueueLoader;
import mgh14.search.live.model.web.ResourceUrlGetter;
import org.apache.commons.io.FileUtils;
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
  private static final String BASE_SAVE_DIRECTORY = "C:\\Users\\mgh14\\Pictures\\";
  private static final String DIRECTORY_TIME_APPENDER = "-time";

  @Autowired
  private ResourceUrlGetter resourceUrlGetter;

  @Autowired
  private ConcurrentLinkedQueue<String> queue;

  @Autowired
  private QueueLoader queueLoader;

  @Autowired
  private WindowsWallpaperSetter setter;

  @Autowired
  private WallpaperDeleter deleter;

  private List<String> filenames = new LinkedList<String>();
  private String absoluteCurrentFilename;
  private String searchStringFolder;
  private int secondsToSleep;

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

    queueLoader.startResourceDownloads();

    // run resource cycle
    runCycle();
  }

  private void runCycle() {
    Log.debug("Starting wallpaper cycle...");
    new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
        long startTime = System.currentTimeMillis();
        while (queue.isEmpty()) {
          if (System.currentTimeMillis() - startTime > SECONDS_TO_TIMEOUT * 1000) {
            Log.info("Waited {} seconds, queue is still empty...exiting",
              SECONDS_TO_TIMEOUT);
            System.exit(0);
          }
        }

          if (isCycleActive.get()) {
            String filename = queue.poll();
            if (canOpenImage(filename)) {
              filenames.add(filename);
              absoluteCurrentFilename = filename;

              // set image to desktop
              setter.setDesktopWallpaper(filename);

              // sleep for x milliseconds (enjoy the background!)
              final long secondsToSleepInMillis = secondsToSleep * 1000;
              final long sleepStartTime = System.currentTimeMillis();
              while (!(getNextResource.get()) &&
                (System.currentTimeMillis() - sleepStartTime) <
                  secondsToSleepInMillis) {
              }
              if (getNextResource.get()) {
                Log.info("Skipping to next resource...");
                setGetNextResource(false);
              }
            }
            else {
              Log.error("Couldn't open file: [{}]. " +
                "Deleting and moving to next resource...", filename);
              deleter.deleteFile(new File(filename).toPath());
            }
          }
        }
      }
    }).start();
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
    final String filename = absoluteCurrentFilename.substring(
      absoluteCurrentFilename.lastIndexOf("\\"));

    try {
      FileUtils.copyFile(new File(absoluteCurrentFilename),
        new File(BASE_SAVE_DIRECTORY + searchStringFolder + filename));
    }
    catch (IOException e) {
      Log.error("IOException copying file: {}", absoluteCurrentFilename, e);
      return null;
    }

    return absoluteCurrentFilename;
  }

  private boolean canOpenImage(String absoluteFilepath) {
    BufferedImage image;
    try {
      image = ImageIO.read(new File(absoluteFilepath));
    }
    catch (Exception e) {
      return false;
    }

    final int pixelTolerance = 5;
    return (image != null && image.getWidth() > pixelTolerance
      && image.getHeight() > pixelTolerance);
  }

  private void setCycleActive(boolean cycleActive) {
    isCycleActive.set(cycleActive);
  }

  private void setGetNextResource(boolean newGetNextResource) {
    getNextResource.set(newGetNextResource);
  }

}
