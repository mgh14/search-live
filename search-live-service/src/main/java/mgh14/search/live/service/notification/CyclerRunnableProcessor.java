package mgh14.search.live.service.notification;

import java.util.Observable;

import mgh14.search.live.model.notification.NotificationProcessor;
import mgh14.search.live.model.observable.messaging.ObserverMessageBuilder;
import mgh14.search.live.model.observable.messaging.ObserverMessageProcessor;
import mgh14.search.live.service.messaging.CycleAction;
import mgh14.search.live.service.resource.cycler.ResourceCyclerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Notification processor for communication between the resource
 * cycler runnable class and the control panel.
 *
 */
@Component
public class CyclerRunnableProcessor extends NotificationProcessor {

  private final Logger Log = LoggerFactory.getLogger(
    getClass().getSimpleName());

  @Autowired
  private ObserverMessageBuilder observerMessageBuilder;

  public CyclerRunnableProcessor() {
    registerNotificationProcessor(getClass().getName(), this);
  }

  @Override
  public void processMessage(String message) {
    Log.debug("Processing message: [{}]", message);
    if (ResourceCyclerRunnable.RESOURCE_CYCLE_STARTED_MESSAGE
      .equals(message)) {

      notifyObserversWithMessage((observerMessageBuilder
        .buildObserverMessage(CycleAction.START_SERVICE.name(),
          ObserverMessageProcessor.MESSAGE_SUCCESS)));
    }
    else if (ResourceCyclerRunnable.RESOURCE_CYCLE_START_FAILED_MESSAGE
      .equals(message)) {

      notifyObserversWithMessage((observerMessageBuilder
        .buildObserverMessage(CycleAction.START_SERVICE.name(),
          ObserverMessageProcessor.MESSAGE_FAILURE)));
    }
    else if (ResourceCyclerRunnable.RESOURCE_SKIPPED_MESSAGE_SUCCESS
      .equals(message)) {

      notifyObserversWithMessage((observerMessageBuilder
        .buildObserverMessage(CycleAction.NEXT.name(),
          ObserverMessageProcessor.MESSAGE_SUCCESS)));
    }
    else if (ResourceCyclerRunnable.RESOURCE_SKIPPED_MESSAGE_FAILURE
      .equals(message)) {

      notifyObserversWithMessage((observerMessageBuilder
        .buildObserverMessage(CycleAction.NEXT.name(),
          ObserverMessageProcessor.MESSAGE_FAILURE)));
    }
  }

  @Override
  public void update(Observable o, Object arg) {
    final String message = (String) arg;
    if (o instanceof ResourceCyclerRunnable) {
      processMessage(message);
    }
  }
}
