package mgh14.search.live.gui;

import mgh14.search.live.model.messaging.CycleAction;
import mgh14.search.live.model.messaging.CycleCommand;
import mgh14.search.live.service.CommandExecutor;

/**
 * UI Controller that gives commands to the model's
 * command executor.
 */
public class GuiController {

  private CommandExecutor executor;

  public void setCommandExecutor(CommandExecutor executor) {
    this.executor = executor;
  }

  public void pauseResourceCycle() {
    executor.addCommandToQueue(new CycleCommand(CycleAction.PAUSE, null));
  }

  public void saveCurrentImage() {
    executor.addCommandToQueue(new CycleCommand(CycleAction.SAVE, null));
  }
}
