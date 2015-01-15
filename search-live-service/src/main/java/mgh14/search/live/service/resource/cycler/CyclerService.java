package mgh14.search.live.service.resource.cycler;

import java.io.File;
import java.util.Observable;
import java.util.concurrent.ExecutorService;

import mgh14.search.live.model.web.util.FileUtils;
import mgh14.search.live.model.wallpaper.QueueLoader;
import mgh14.search.live.model.web.resource.getter.ResourceUrlGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service responsible for running the runnable resource cycler
 * code. This class serves as the service layer between the
 * command executor and the remainder of the classes.
 */
@Component
public class CyclerService extends Observable {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  private static final String DIRECTORY_TIME_APPENDER = "-time";

  @Autowired
  private ResourceCyclerRunnable resourceCyclerRunnable;
  @Autowired
  private RetryTimerRunnable retryTimerRunnable;
  @Autowired
  private ExecutorService executorService;
  @Autowired
  private ResourceUrlGetter resourceUrlGetter;
  @Autowired
  private QueueLoader queueLoader;
  @Autowired
  private FileUtils fileUtils;

  private String searchStringFolder;

  public CyclerService() {
    searchStringFolder = null;
  }

  public void setSecondsToSleep(int secondsToSleep) {
    resourceCyclerRunnable.setSecondsToSleep(secondsToSleep);
  }

  public void startService(final String searchString) {
    if (searchString == null || searchString.isEmpty()) {
      Log.error("Please enter a search query (e.g. \"desktop wallpaper\"");
      return;
    }
    resourceUrlGetter.setSearchString(searchString);
    searchStringFolder = searchString.replace(" ", "-") + DIRECTORY_TIME_APPENDER
      + System.currentTimeMillis() + "\\";

    // run resource cycle
    runResourceCycle();
  }

  private void runResourceCycle() {
    Log.debug("Starting wallpaper cycle...");
    queueLoader.startResourceDownloads();
    runRetryTimer();

    executorService.execute(resourceCyclerRunnable);
  }

  public String saveCurrentImage() {
    return resourceCyclerRunnable.saveCurrentImage(
      searchStringFolder);
  }

  public void pauseCycle() {
    Log.debug("Pausing resource cycle...");
    resourceCyclerRunnable.setIsCycleActive(false);
  }

  public void resumeCycle() {
    Log.debug("Resuming resource cycle...");
    resourceCyclerRunnable.setIsCycleActive(true);
  }

  public void getNextResource() {
    Log.debug("Getting next resource...");
    resourceCyclerRunnable.setGetNextResource(true);
  }

  public void deleteAllResources() {
    Log.debug("Deleting all resources...");
    fileUtils.deleteAllFiles(new File(fileUtils.getResourceFolder()));
  }

  private void runRetryTimer() {
    Log.debug("Starting retry timer...");
    executorService.execute(retryTimerRunnable);
  }

  private void notifyObserversWithMessage(String message) {
    setChanged();
    notifyObservers(message);
  }

}