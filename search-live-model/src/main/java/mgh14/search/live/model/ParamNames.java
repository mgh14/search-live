package mgh14.search.live.model;

import org.springframework.stereotype.Component;

/**
 * Class that contains parameter names for the model's
 * key-value pairs (e.g. preferences or config properties).
 */
@Component
public class ParamNames {

  private ParamNames() {}

  // System property param names
  public static final String USER_HOME = "user.home";

  // Preferences param names
  public static final String RESOURCE_SAVE_DIR = "resource-save-dir";

}
