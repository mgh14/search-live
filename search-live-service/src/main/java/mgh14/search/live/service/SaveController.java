package mgh14.search.live.service;

/**
 * UI Controller that gives commands to the model's
 * command executor.
 */
public class SaveController {

  private CommandExecutor executor;

  public void setCommandExecutor(CommandExecutor executor) {
    this.executor = executor;
  }

  /*public String saveCurrentImage() {
    return cycler.saveCurrentImage();
  }*/
}
