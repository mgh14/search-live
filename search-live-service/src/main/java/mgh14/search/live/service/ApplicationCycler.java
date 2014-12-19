package mgh14.search.live.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.imageio.ImageIO;

import mgh14.search.live.model.WallpaperDeleter;
import mgh14.search.live.model.WindowsWallpaperSetter;
import mgh14.search.live.model.web.QueueLoader;
import mgh14.search.live.model.web.ResourceUrlGetter;
import org.apache.commons.io.FileUtils;

/**
 * Class that cycles desktop wallpaper resources
 */
public class ApplicationCycler {


  private static final int SECONDS_TO_TIMEOUT = 30;
  private static final String SAVE_DIRECTORY = "C:\\Users\\mgh14\\Pictures\\";

  private ResourceUrlGetter resourceUrlGetter;
  private List<String> filenames = new LinkedList<String>();
  private ConcurrentLinkedQueue<String> queue;
  private QueueLoader queueLoader;
  private String absoluteCurrentFilename;
  private WallpaperDeleter deleter;

  public ApplicationCycler(final ResourceUrlGetter resourceUrlGetter) {
    this.resourceUrlGetter = resourceUrlGetter;
    queue = new ConcurrentLinkedQueue<String>();

    queueLoader = new QueueLoader();
    queueLoader.setQueue(queue);

    absoluteCurrentFilename = null;

    deleter = new WallpaperDeleter();
  }

  public void startCycle(final String searchString, final int secondsToSleep) {
    if(searchString == null || searchString.isEmpty()) {
      System.out.println("Please enter a search query (e.g. \"desktop wallpaper\"");
      return;
    }
    resourceUrlGetter.setSearchString(searchString);

    queueLoader.startResourceDownloads(resourceUrlGetter);

    // run wallpaper cycle
    WindowsWallpaperSetter setter = new WindowsWallpaperSetter();
    System.out.println("Starting wallpaper cycle...");
    while (true) {
      long startTime = System.currentTimeMillis();
      while (queue.isEmpty()) {
        if (System.currentTimeMillis() - startTime > SECONDS_TO_TIMEOUT * 1000) {
          System.out.println("Waited " + SECONDS_TO_TIMEOUT +
            " seconds, queue is still empty...exiting");
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
      } else {
        System.out.println("Couldn\'t open file: [" + filename + "]. " +
          "Deleting and moving to next resource...");
        deleter.deleteFile(new File(filename).toPath());
      }
    }

  }

  public boolean canOpenImage(String absoluteFilepath) {
    BufferedImage image;
    try {
      image = ImageIO.read(new File(absoluteFilepath));
    } catch (Exception e) {
      return false;
    }

    final int pixelTolerance = 5;
    return (image != null && image.getWidth() > pixelTolerance
      && image.getHeight() > pixelTolerance);

    /*for (int i=0; i<image.getWidth(); i++) {
      for (int j=0; j<image.getHeight(); j++) {
        System.out.println(image.getRGB(i, j));
      }
    }*/
  }

  public String saveCurrentImage() {
    final String filename = absoluteCurrentFilename.substring(
      absoluteCurrentFilename.lastIndexOf("\\"));

    try {
      FileUtils.copyFile(new File(absoluteCurrentFilename),
        new File(SAVE_DIRECTORY + filename));
    }
    catch (IOException e) {
      System.out.println("IOException copying file: " + absoluteCurrentFilename);
      return null;
    }

    return absoluteCurrentFilename;
  }

  private void sleep(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    }
    catch (InterruptedException e) {
      System.out.println("Interrupted cycle");
      e.printStackTrace();
    }
  }

}
