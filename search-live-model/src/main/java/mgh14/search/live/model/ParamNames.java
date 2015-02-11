package mgh14.search.live.model;

import org.springframework.stereotype.Component;

/**
 * Class that contains parameter names for the model's
 * key-value pairs (e.g. preferences or config properties).
 */
@Component
public final class ParamNames {

  private ParamNames() {}

  // System property param names
  public static final String USER_HOME = "user.home";

  // Config property param names
  public static final String TEMP_RESOURCES_DIR = "temp-resources-dir";

  // Preferences param names
  public static final String RESOURCE_SAVE_DIR = "resource-save-dir";
  public static final String NUM_SECONDS_BETWEEN_CYCLES = "cycle-seconds";
  public static final String LAST_SEARCH = "last-search";

}
