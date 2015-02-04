package mgh14.search.live.model.notification;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Abstract class extendable for intermediate observer-observable
 * message processing. This class will allow different components in
 * different layers of applications to communicate without cluttering
 * up class functionality.
 */
public abstract class NotificationProcessor extends Observable
  implements Observer {

  private static HashMap<String, NotificationProcessor>
    notificationProcessors = new HashMap<String,
    NotificationProcessor>();

  final public void registerNotificationProcessor(String name,
    NotificationProcessor notificationProcessor) {
    notificationProcessors.put(name, notificationProcessor);
  }

  final NotificationProcessor getNotificationProcessor(Class classType) {
    return (notificationProcessors.containsKey(classType.getName())) ?
      notificationProcessors.get(classType.getName()) : null;
  }

  public void setObservers(Observer... observers) {
    for (Observer observer : observers) {
      addObserver(observer);
    }
  }

  final public void notifyObserversWithMessage(String message) {
    setChanged();
    notifyObservers(message);
  }

  abstract public void processMessage(String message);

}
