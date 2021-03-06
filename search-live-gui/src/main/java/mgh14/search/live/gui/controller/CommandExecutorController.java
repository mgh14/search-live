package mgh14.search.live.gui.controller;

import java.util.Observable;
import java.util.Observer;
import javax.annotation.PostConstruct;

import mgh14.search.live.gui.ControlPanel;
import mgh14.search.live.model.notification.NotificationProcessor;
import mgh14.search.live.model.observable.messaging.ObserverMessageProcessor;
import mgh14.search.live.service.CommandExecutor;
import mgh14.search.live.service.messaging.CycleAction;
import mgh14.search.live.service.messaging.CycleCommand;
import mgh14.search.live.service.resource.cycler.ResourceCyclerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UI Controller that gives commands to the service's
 * command executor and also updates the UI with
 * messages from the service.
 */
@Component
public class CommandExecutorController implements Observer {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());
  private static final String ERROR_PREFIX = "<b>Error: </b>";

  @Autowired
  private CommandExecutor executor;
  @Autowired
  private ControlPanel controlPanel;
  @Autowired
  private ObserverMessageProcessor observerMessageProcessor;

  @Autowired
  private NotificationProcessor cyclerServiceProcessor;

  @PostConstruct
  public void registerWithResourceCycler() {
    cyclerServiceProcessor.addObserver(this);
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
    Log.debug("Controller receiving notification from {} with " +
      "arg [{}]", o.getClass().getSimpleName(), arg);

    processMessage((String) arg);
  }

  private void processMessage(String message) {
    observerMessageProcessor.setResponseMessage(message);
    final String messageStatusType = observerMessageProcessor
      .getStatusType();

    String guiMessage = "";
    if (messageStatusType.equals(CycleAction.START_SERVICE.name())) {
      guiMessage = (observerMessageProcessor.isSuccessMessage()) ?
        "Cycle started" : ERROR_PREFIX + "cycle not started!";
    }
    if (messageStatusType.equals(CycleAction.PAUSE.name())) {
      guiMessage = (observerMessageProcessor.isSuccessMessage()) ?
        "Cycle paused" : ERROR_PREFIX + "cycle not paused!";
    }
    if (messageStatusType.equals(CycleAction.RESUME.name())) {
      guiMessage = (observerMessageProcessor.isSuccessMessage()) ?
        "Cycle resumed" : ERROR_PREFIX + "cycle not resumed!";
    }
    if (messageStatusType.equals(CycleAction.SAVE.name())) {
      guiMessage = (observerMessageProcessor.isSuccessMessage() ?
        "Saved: " + observerMessageProcessor.getArgument(0) :
        ERROR_PREFIX + observerMessageProcessor.getArgument(0) +
          "\nnot saved!");
    }
    if (messageStatusType.equals(CycleAction.NEXT.name())) {
      guiMessage = (observerMessageProcessor.isSuccessMessage()) ?
        "Skipped: " + observerMessageProcessor.getArgument(0) :
        ERROR_PREFIX + "resource not skipped!";
    }
    if (messageStatusType.equals(CycleAction.DELETE_RESOURCES.name())) {
      guiMessage = (observerMessageProcessor.isSuccessMessage() ?
        "All resources deleted" : ERROR_PREFIX + "resources couldn't " +
        "be deleted!");
    }
    if (messageStatusType.equals(ResourceCyclerRunnable.SLEEP_FINISHED)) {
      guiMessage = (observerMessageProcessor.isSuccessMessage() ?
      "Getting next resource..." : ERROR_PREFIX + "sleep didn't cycle" +
        "the resource!");
    }
    if (messageStatusType.equals(ResourceCyclerRunnable.RESOURCE_SET)) {
      guiMessage = (observerMessageProcessor.isSuccessMessage() ?
        "" : ERROR_PREFIX + "resource setter failed!");
    }

    if (observerMessageProcessor.isSuccessMessage()) {
      controlPanel.setStatusText(guiMessage);
    }
    else {
      controlPanel.setErrorStatusText(guiMessage);
    }
    controlPanel.refreshButtons();
  }

}
