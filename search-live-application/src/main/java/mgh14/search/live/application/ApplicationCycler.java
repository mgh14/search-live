package mgh14.search.live.application;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import mgh14.search.live.model.WindowsWallpaperSetter;
import mgh14.search.live.web.QueueLoader;
import mgh14.search.live.web.ResourceUrlGetter;

/**
 * Class that cycles desktop wallpaper resources
 */
public class ApplicationCycler {


  private static final int SECONDS_TO_TIMEOUT = 30;

  private ResourceUrlGetter resourceUrlGetter;
  private List<String> filenames = new LinkedList<String>();
  private ConcurrentLinkedQueue<String> queue;
  private QueueLoader queueLoader;
  public ApplicationCycler(final ResourceUrlGetter resourceUrlGetter) {
    this.resourceUrlGetter = resourceUrlGetter;
    queue = new ConcurrentLinkedQueue<String>();

    queueLoader = new QueueLoader();
    queueLoader.setQueue(queue);
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
      filenames.add(filename);

      // set image to desktop
      setter.setDesktopWallpaper(filename);

      // sleep for x milliseconds (enjoy the background!)
      sleep(secondsToSleep * 1000);
    }

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
