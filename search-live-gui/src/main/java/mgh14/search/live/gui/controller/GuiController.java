package mgh14.search.live.gui.controller;

import java.util.Observable;
import java.util.Observer;
import javax.annotation.PostConstruct;

import mgh14.search.live.gui.ControlPanel;
import mgh14.search.live.service.CommandExecutor;
import mgh14.search.live.service.resource.cycler.CyclerService;
import mgh14.search.live.service.messaging.CycleAction;
import mgh14.search.live.service.messaging.CycleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UI Controller that gives commands to the model's
 * command executor.
 */
@Component
public class GuiController implements Observer {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private CommandExecutor executor;
  @Autowired
  private CyclerService cyclerService;
  @Autowired
  private ControlPanel controlPanel;

  @PostConstruct
  public void registerWithResourceCycler() {
    cyclerService.addObserver(this);
  }

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

  @Override
  public void update(Observable o, Object arg) {
    // TODO: Implement(!)
    Log.debug("Controller receiving notification from {} with " +
      "arg [{}]", o, arg);
    if (o instanceof CyclerService) {
      controlPanel.setStatusText((String) arg);
    }
  }
}
