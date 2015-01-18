package mgh14.search.live.service.messaging;

import org.springframework.stereotype.Component;

/**
 * status-type:success:argument[:argument]
 */
@Component
public class ObserverMessageProcessor {

  private static final int MESSAGE_SUCCESS_INDEX = 1;
  private static final String MESSAGE_SUCCESS = "success";
  private static final String MESSAGE_FAILURE = "failure";

  private String[] currentResponseMessage = null;

  public void setResponseMessage(String message) throws IllegalArgumentException {
    if (message == null || message.isEmpty()) {
      throw new IllegalArgumentException("Message has null content!");
    }

    final String[] responseMessage = message.split(":");
    if (responseMessage.length < 2) {
      throw new IllegalArgumentException("Message is not formatted " +
        "properly!");
    }
    currentResponseMessage = responseMessage;

  }

  public boolean isSuccessMessage() throws IllegalStateException {
    if (currentResponseMessage == null) {
      throw new IllegalStateException("Current response message is null");
    }
    return currentResponseMessage[MESSAGE_SUCCESS_INDEX].equals(
      MESSAGE_SUCCESS);
  }

  public String getStatusType() {
    if (currentResponseMessage == null) {
      return null;
    }

    return currentResponseMessage[0];
  }

  public String getArgument(int argIndex) {
    if (currentResponseMessage == null) {
      return null;
    }

    return (currentResponseMessage.length > 2) ?
      currentResponseMessage[argIndex + 2] : null;
  }

}
