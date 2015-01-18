package mgh14.search.live.service.messaging;

import org.springframework.stereotype.Component;

/**
 * Class for creating observer messages
 */
@Component
public class ObserverMessageBuilder {

  public static final String SEPARATOR = "::";

  public String buildObserverMessage(String... args) {
    String observerMessage = "";
    for (String arg : args) {
      observerMessage += arg + SEPARATOR;
    }

    return observerMessage.substring(0,
      observerMessage.length() - SEPARATOR.length());
  }

}
