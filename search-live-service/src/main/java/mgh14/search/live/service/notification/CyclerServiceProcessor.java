package mgh14.search.live.service.notification;

import java.util.Observable;

import mgh14.search.live.model.notification.NotificationProcessor;
import mgh14.search.live.model.observable.messaging.ObserverMessageProcessor;
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

  public CyclerServiceProcessor() {
    registerNotificationProcessor(getClass().getName(), this);
  }

  @Override
  public void processMessage(String message) {
    Log.debug("Processing message: [{}]", message);

    // make sure the message can be processed (i.e. the
    // message is valid)
    observerMessageProcessor.setResponseMessage(message);

    notifyObserversWithMessage(message);
  }

  @Override
  public void update(Observable o, Object arg) {
    final String message = (String) arg;
    if (o instanceof ResourceCyclerRunnable ||
      o instanceof CyclerService) {
      processMessage(message);
    }
  }

}
