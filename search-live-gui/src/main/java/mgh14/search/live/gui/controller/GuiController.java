package mgh14.search.live.gui.controller;

import mgh14.search.live.service.CommandExecutor;
import mgh14.search.live.service.messaging.CycleAction;
import mgh14.search.live.service.messaging.CycleCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UI Controller that gives commands to the model's
 * command executor.
 */
@Component
public class GuiController {

  @Autowired
  private CommandExecutor executor;

  public void startResourceCycle(String query) {
    executor.addCommandToQueue(new CycleCommand(CycleAction.START_SERVICE,
      "searchString:" + query));
  }

  public void pauseResourceCycle() {
    executor.addCommandToQueue(new CycleCommand(CycleAction.PAUSE));
  }

  public void resumeResourceCycle() {
    executor.addCommandToQueue(new CycleCommand(CycleAction.RESUME));
  }

  public void cycleNextResource() {
    executor.addCommandToQueue(new CycleCommand(CycleAction.NEXT));
  }

  public void saveCurrentImage() {
    executor.addCommandToQueue(new CycleCommand(CycleAction.SAVE));
  }

  public void deleteAllResources() {
    executor.addCommandToQueue(new CycleCommand(CycleAction.DELETE_RESOURCES));
  }

  public void shutdownApplication() {
    executor.addCommandToQueue(new CycleCommand(CycleAction.SHUTDOWN));
  }
}
