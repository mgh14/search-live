package mgh14.search.live.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import mgh14.search.live.model.messaging.CycleAction;
import mgh14.search.live.model.messaging.CycleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for executing commands on the resource cycler
 */
public class CommandExecutor {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  private ResourceCycler resourceCycler;
  private Queue<CycleCommand> commandQueue;

  public CommandExecutor() {
    commandQueue = new ConcurrentLinkedQueue<CycleCommand>();
  }

  public void setResourceCycler(ResourceCycler resourceCycler) {
    this.resourceCycler = resourceCycler;
  }

  public void addCommandToQueue(CycleCommand newCommand) {
    if (newCommand == null) {
      throw new IllegalArgumentException("Command for executor can't be null");
    }
    commandQueue.add(newCommand);
  }

  public void run() {
    while (true) {
      if (commandQueue.isEmpty()) {
        continue;
      }

      final CycleCommand command = commandQueue.poll();
      final CycleAction action = command.getCycleAction();
      Log.debug("Processing command: command=[{}], body=[{}]", action, command.getBody());
      if (CycleAction.START.equals(action)) {
        processStart(command.getBody());
      }
      if (CycleAction.PAUSE.equals(action)) {
        Log.info("Pausing cycle...");
        resourceCycler.pauseCycle();
      }
      if (CycleAction.SAVE.equals(action)) {
        processSave();
      }
    }
  }

  private void processStart(String commandBody) {
    Log.info("Starting resource cycle...");
    final Map<String, String> properties = getPropsFromBody(commandBody);

    new Thread(new Runnable() {
      @Override
      public void run() {
        resourceCycler.startCycle(properties.get("searchString"),
          Integer.parseInt(properties.get("secondsToSleep")));
      }
    }).start();
  }

  private void processSave() {
    Log.info("Image saved: [{}]", resourceCycler.saveCurrentImage());

    //TODO: How will UI be notified now?
  }

  private Map<String, String> getPropsFromBody(String body) {
    final Map properties = new HashMap<String, String>();

    String[] rawProps = body.split(";");
    for (String prop : rawProps) {
      final int indexOfSeparator = prop.indexOf(":");
      properties.put(prop.substring(0, indexOfSeparator), prop.substring(indexOfSeparator + 1));
    }

    return properties;
  }


}
