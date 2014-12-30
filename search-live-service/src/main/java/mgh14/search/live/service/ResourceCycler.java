package mgh14.search.live.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
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
  private WallpaperDeleter deleter;

  private List<String> filenames = new LinkedList<String>();
  private String absoluteCurrentFilename;
  private String searchStringFolder;

  private boolean cycleRunning;

  public ResourceCycler() {
    absoluteCurrentFilename = null;
    searchStringFolder = null;
  }

  public void startCycle(final String searchString, final int secondsToSleep) {
    cycleRunning = true;
    if(searchString == null || searchString.isEmpty()) {
      Log.error("Please enter a search query (e.g. \"desktop wallpaper\"");
      return;
    }
    resourceUrlGetter.setSearchString(searchString);
    searchStringFolder = searchString.replace(" ", "-") + DIRECTORY_TIME_APPENDER
      + System.currentTimeMillis() + "\\";

    queueLoader.startResourceDownloads(resourceUrlGetter);

    // run wallpaper cycle
    WindowsWallpaperSetter setter = new WindowsWallpaperSetter();
    Log.debug("Starting wallpaper cycle...");
    while (true) {
      if (cycleRunning) {
        long startTime = System.currentTimeMillis();
        while (queue.isEmpty()) {
          if (System.currentTimeMillis() - startTime > SECONDS_TO_TIMEOUT * 1000) {
            Log.info("Waited {} seconds, queue is still empty...exiting",
              SECONDS_TO_TIMEOUT);
            System.exit(0);
          }
        }

        final String filename = queue.poll();
        if (canOpenImage(filename)) {
          filenames.add(filename);
          absoluteCurrentFilename = filename;

          // set image to desktop
          setter.setDesktopWallpaper(filename);

          // sleep for x milliseconds (enjoy the background!)
          sleep(secondsToSleep * 1000);
        }
        else {
          Log.error("Couldn't open file: [{}]. " +
            "Deleting and moving to next resource...", filename);
          deleter.deleteFile(new File(filename).toPath());
        }
      }
    }

  }

  public void pauseCycle() {
    cycleRunning = false;
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
    } catch (Exception e) {
      return false;
    }

    final int pixelTolerance = 5;
    return (image != null && image.getWidth() > pixelTolerance
      && image.getHeight() > pixelTolerance);
  }

  private void sleep(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    }
    catch (InterruptedException e) {
      Log.debug("Interrupted sleep cycle", e);
    }
  }

}
