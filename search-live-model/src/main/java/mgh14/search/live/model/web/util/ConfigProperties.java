package mgh14.search.live.model.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import mgh14.search.live.model.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for loading configuration properties
 */
@Component
public class ConfigProperties {

  public static final String APP_HOME_PARAM = "SEARCH_LIVE_HOME";

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private FileUtils fileUtils;

  private String configDir;
  private Properties properties;

  public ConfigProperties() {
    configDir = null;
    properties = new Properties();
  }

  @PostConstruct
  public void loadConfig() {
    final String appHome = System.getenv().get(APP_HOME_PARAM);
    if (appHome == null || appHome.isEmpty()) {
      Log.error("Error: System application home variable " +
        APP_HOME_PARAM + "is not set; cannot locate config. " +
        "Exiting...");
      System.exit(-1);
    }
    configDir = fileUtils.constructFilepathWithSeparator(
      "search-live-application", "src", "main", "resources",
      "config");

    try {
      loadPropertyValues(configDir);
    }
    catch (IOException e) {
      Log.warn("Warning: Couldn\'t load properties file " +
        "in dir [{}]. Continuing...", configDir);
    }
  }

  public Set<Object> getPropertyKeys() {
    return properties.keySet();
  }

  public Object getProperty(String propertyName) {
    return properties.get(propertyName);
  }

  public void setProperty(String propertyName, String value) {
    properties.put(propertyName, value);
  }

  private void loadPropertyValues(String dirLocation) throws IOException {
    final String fullFilepath = dirLocation + "config.properties";
    final InputStream inputStream = new FileInputStream(new File(fullFilepath));
    properties.load(inputStream);

    inputStream.close();
  }

}
