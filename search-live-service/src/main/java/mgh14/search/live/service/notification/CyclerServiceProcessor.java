package mgh14.search.live.service.notification;

import java.util.Observable;

import mgh14.search.live.model.notification.NotificationProcessor;
import mgh14.search.live.model.observable.messaging.ObserverMessageBuilder;
import mgh14.search.live.model.observable.messaging.ObserverMessageProcessor;
import mgh14.search.live.model.web.util.FileUtils;
import mgh14.search.live.service.messaging.CycleAction;
import mgh14.search.live.service.resource.cycler.CyclerService;
import mgh14.search.live.service.resource.cycler.ResourceCyclerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Notification processor for the cycler service and the resource
 * cycler runnable classes.
 */
@Component
public class CyclerServiceProcessor extends NotificationProcessor {

  private final Logger Log = LoggerFactory.getLogger(
    getClass().getSimpleName());

  @Autowired
  private ObserverMessageProcessor observerMessageProcessor;
  @Autowired
  private ObserverMessageBuilder observerMessageBuilder;

  public CyclerServiceProcessor() {
    registerNotificationProcessor(getClass().getName(), this);
  }

  @Override
  public void processMessage(String message) {
    Log.debug("Processing message: [{}]", message);

    // make sure the message can be processed (i.e. the
    // message is valid)
    observerMessageProcessor.setResponseMessage(message);
    final String messageStatusType = observerMessageProcessor
      .getStatusType();
    if (FileUtils.DELETE_RESOURCES_IDENTIFIER.equals(
      messageStatusType)) {
      processFileUtilsDelete(message);
    }
    else {
      notifyObserversWithMessage(message);
    }
  }

  private void processFileUtilsDelete(String message) {
    observerMessageProcessor.setResponseMessage(message);

    final String successString = (observerMessageProcessor
      .isSuccessMessage()) ?
      ObserverMessageProcessor.MESSAGE_SUCCESS :
      ObserverMessageProcessor.MESSAGE_FAILURE;

    notifyObserversWithMessage(observerMessageBuilder
      .buildObserverMessage(CycleAction.DELETE_RESOURCES.name(),
      successString));
  }

  @Override
  public void update(Observable o, Object arg) {
    final String message = (String) arg;
    if (o instanceof ResourceCyclerRunnable ||
      o instanceof CyclerService ||
      o instanceof FileUtils) {
      processMessage(message);
    }
  }

}
