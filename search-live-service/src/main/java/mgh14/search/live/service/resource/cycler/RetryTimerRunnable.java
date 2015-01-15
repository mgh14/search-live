package mgh14.search.live.service.resource.cycler;

import java.util.concurrent.ConcurrentLinkedQueue;

import mgh14.search.live.model.wallpaper.QueueLoader;
import mgh14.search.live.service.CommandExecutor;
import mgh14.search.live.service.messaging.CycleAction;
import mgh14.search.live.service.messaging.CycleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for running the timer that periodically kicks
 * off downloads to fill its resource queue.
 */
@Component
class RetryTimerRunnable implements Runnable {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  private static final int NUM_RETRIES_BEFORE_STOP = 5;
  private static final int RESOURCE_QUEUE_THRESHOLD = 2;

  @Autowired
  private QueueLoader queueLoader;
  @Autowired
  private ConcurrentLinkedQueue<String> resourcesQueue;
  @Autowired
  private CommandExecutor commandExecutor;

  @Override
  public void run() {
    int retryCount = 0;
    long timeOfLastRetry = System.currentTimeMillis();

    while (true) {
      if (resourcesQueue.size() < RESOURCE_QUEUE_THRESHOLD &&
        !queueLoader.isDownloading()) {

        final long timeElapsed = System.currentTimeMillis() -
          timeOfLastRetry;
        if (retryCount < NUM_RETRIES_BEFORE_STOP && timeElapsed > 3000) {
          Log.debug("Timer kicking off retry {}...", (retryCount + 1));
          queueLoader.startResourceDownloads();
          retryCount++;
          timeOfLastRetry = System.currentTimeMillis();
        }
        else if (retryCount >= NUM_RETRIES_BEFORE_STOP) {
          Log.info("Retry count reached. Sending exit command...");
          commandExecutor.addCommandToQueue(new CycleCommand(
            CycleAction.SHUTDOWN));
          return;   // terminate thread
        }
      }
      else if (resourcesQueue.size() >= RESOURCE_QUEUE_THRESHOLD) {
        retryCount = 0;
        timeOfLastRetry = System.currentTimeMillis();
      }
    }
  }

}
