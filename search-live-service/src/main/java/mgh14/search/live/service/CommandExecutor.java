package mgh14.search.live.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import mgh14.search.live.service.messaging.CycleAction;
import mgh14.search.live.service.messaging.CycleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for executing commands on the resource cycler
 */
@Component
public class CommandExecutor {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());
  private static final int NUM_SECONDS_TO_WAIT_FOR_SHUTDOWN = 5;

  @Autowired
  private ExecutorService executorService;
  @Autowired
  private ResourceCycler resourceCycler;
  private Queue<CycleCommand> commandQueue;

  public CommandExecutor() {
    commandQueue = new ConcurrentLinkedQueue<CycleCommand>();
  }

  public void addCommandToQueue(CycleCommand newCommand) {
    if (newCommand == null) {
      throw new IllegalArgumentException("Command for executor can't be null");
    }
    commandQueue.add(newCommand);
  }

  public void run() {
    Log.info("Starting command queue...");
    while (true) {
      if (commandQueue.isEmpty()) {
        continue;
      }

      final CycleCommand command = commandQueue.poll();
      final CycleAction action = command.getCycleAction();
      Log.debug("Processing command: command=[{}], body=[{}]", action, command.getBody());
      if (CycleAction.START_SERVICE.equals(action)) {
        processStart(command.getBody());
      }
      if (CycleAction.PAUSE.equals(action)) {
        Log.info("Pausing cycle...");
        resourceCycler.pauseCycle();
      }
      if (CycleAction.RESUME.equals(action)) {
        Log.info("Resuming cycle...");
        resourceCycler.resumeCycle();
      }
      if (CycleAction.NEXT.equals(action)) {
        processNext();
      }
      if (CycleAction.SAVE.equals(action)) {
        processSave();
      }
      if (CycleAction.DELETE_RESOURCES.equals(action)) {
        processDeleteResourceCache();
      }
      if (CycleAction.SHUTDOWN.equals(action)) {
        processShutdown();
        break;
      }
    }
  }

  private void processStart(String commandBody) {
    Log.info("Starting resource cycle...");
    final Map<String, String> properties = getPropertiesFromBody(commandBody);

    executorService.execute(new Runnable() {
      @Override
      public void run() {
        resourceCycler.startCycle(properties.get("searchString"));
      }
    });
  }

  private void processNext() {
    Log.info("Getting next resource in the cycle...");
    resourceCycler.getNextResource();
  }

  private void processSave() {
    Log.info("Image saved: [{}]", resourceCycler.saveCurrentImage());
  }

  private void processDeleteResourceCache() {
    Log.info("Deleting resource cache...");
    resourceCycler.deleteAllResources();
  }

  private void processShutdown() {
    Log.debug("Shutting down application...");

    Log.debug("Shutting down executor service...");
    executorService.shutdown();
    Log.debug("Waiting 10 seconds for threads to terminate...");
    try {
      Thread.sleep(NUM_SECONDS_TO_WAIT_FOR_SHUTDOWN * 1000);
    }
    catch (InterruptedException e) {
      Log.error("Interrupt: ", e);
    }

    Log.debug("Finished application shutdown.");
    System.exit(0);
  }

  private Map<String, String> getPropertiesFromBody(String body) {
    final Map<String, String> properties = new HashMap<String, String>();

    String[] rawProps = body.split(";");
    for (String prop : rawProps) {
      final int indexOfSeparator = prop.indexOf(":");
      properties.put(prop.substring(0, indexOfSeparator),
        prop.substring(indexOfSeparator + 1));
    }

    return properties;
  }

}
