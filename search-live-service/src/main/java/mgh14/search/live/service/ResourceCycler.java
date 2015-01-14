package mgh14.search.live.service;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import mgh14.search.live.model.FileUtils;
import mgh14.search.live.model.wallpaper.QueueLoader;
import mgh14.search.live.model.wallpaper.WindowsWallpaperSetter;
import mgh14.search.live.model.web.resource.getter.ResourceUrlGetter;
import mgh14.search.live.model.web.util.ImageUtils;
import mgh14.search.live.service.messaging.CycleAction;
import mgh14.search.live.service.messaging.CycleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that cycles desktop wallpaper resources
 */
@Component
public class ResourceCycler extends Observable {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  private static final int DEFAULT_SECONDS_TO_SLEEP = 300;
  private static final int QUEUE_TIMEOUT_SECONDS = 30;
  private static final int QUEUE_TIMEOUT_MILLISECONDS =
    QUEUE_TIMEOUT_SECONDS * 1000;
  private static final String DIRECTORY_TIME_APPENDER = "-time";

  @Autowired
  private CommandExecutor commandExecutor;
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
  private FileUtils fileUtils;
  @Autowired
  private ImageUtils imageUtils;

  private List<String> filenames = new LinkedList<String>();
  private String currentAbsoluteFilename;
  private String searchStringFolder;
  private AtomicInteger secondsToSleep;

  private AtomicBoolean isCycleActive;
  private AtomicBoolean getNextResource;

  public ResourceCycler() {
    currentAbsoluteFilename = null;
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
        queueLoader.startResourceDownloads();

        while (true) {
          long startTime = System.currentTimeMillis();
          while (resourcesQueue.isEmpty()) {
            final long currentTimeMillis = System.currentTimeMillis();
            final long elapsedTimeMillis = currentTimeMillis - startTime;
            // TODO: Implement retries here instead of queue timeout?
            if (elapsedTimeMillis > QUEUE_TIMEOUT_MILLISECONDS) {
                Log.info("Empty queue timeout of {} seconds reached. Sending exit command...",
                  (QUEUE_TIMEOUT_SECONDS));
                commandExecutor.addCommandToQueue(new CycleCommand(CycleAction.SHUTDOWN));
                return;   // terminate thread
            }
            // TODO: else if ((elapsedTimeMillis % 6000 > 5000) && !queueLoader.isDownloading()) {
            else if (!queueLoader.isDownloading()) {
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
              currentAbsoluteFilename = filename;

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
    });
  }

  public String saveCurrentImage() {
    return imageUtils.saveImage(searchStringFolder,
      currentAbsoluteFilename);
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
    fileUtils.deleteAllFiles(new File(fileUtils.getResourceFolder()));
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

  private void notifyObserversWithMessage(String message) {
    setChanged();
    notifyObservers(message);
  }

}
