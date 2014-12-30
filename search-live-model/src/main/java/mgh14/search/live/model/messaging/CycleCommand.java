package mgh14.search.live.model.messaging;

/**
 * Medium for communicating commands and message bodies
 * between the UI and the model.
 */
public class CycleCommand {

  private CycleAction cycleAction;
  private String body;

  public CycleCommand() {
    this(CycleAction.NOP, null);
  }

  public CycleCommand(String body) {
    this(null, body);
  }

  public CycleCommand(CycleAction cycleAction, String body) {
    setCycleAction(cycleAction);
    setBody(body);
  }

  public CycleAction getCycleAction() {
    return cycleAction;
  }

  public void setCycleAction(CycleAction cycleAction) {
    this.cycleAction = cycleAction;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof CycleCommand)) {
      return false;
    }

    final CycleCommand otherCommand = (CycleCommand) o;
    final CycleAction otherCycleAction = otherCommand.getCycleAction();
    final String otherBody = otherCommand.getBody();

    return !((cycleAction == null && otherCycleAction != null) ||
      (cycleAction != null && otherCycleAction == null) ||
      (body == null && otherBody != null) ||
      (body != null && otherBody == null)) &&
      ((getBody().equals(otherCommand.getBody()) &&
        (getCycleAction().equals(otherCommand.getCycleAction()))));

  }

}