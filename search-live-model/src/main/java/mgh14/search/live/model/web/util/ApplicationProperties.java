package mgh14.search.live.model.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
public class ApplicationProperties {

  public static final String APP_HOME_PARAM = "SEARCH_LIVE_HOME";

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private FileUtils fileUtils;

  private String configDir;
  private Properties configProperties;
  private String prefsDir;
  private Properties prefsProperties;

  public ApplicationProperties() {
    configDir = null;
    configProperties = new Properties();
    prefsDir = null;
    prefsProperties = new Properties();
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
      loadPropertyValues(configDir, "config.properties", configProperties);
    }
    catch (IOException e) {
      Log.warn("Warning: Couldn\'t load properties file " +
        "in dir [{}]. Continuing...", configDir);
    }
  }

  @PostConstruct
  public void loadPrefs() {
    final String appHome = System.getenv().get(APP_HOME_PARAM);
    if (appHome == null || appHome.isEmpty()) {
      Log.warn("Error: System application home variable" +
        APP_HOME_PARAM + "is not set; cannot locate prefs.");
    }
    prefsDir = fileUtils.constructFilepathWithSeparator(
      "search-live-application", "src", "main", "resources",
      "prefs");

    try {
      loadPropertyValues(prefsDir, "prefs.properties", prefsProperties);
    } catch (IOException e) {
      Log.warn("Warning: Couldn't load prefs properties file " +
        "in dir [{}]. Continuing...", prefsDir);
    }
  }

  public Object getConfigProperty(String propertyName) {
    return configProperties.get(propertyName);
  }

  public void setConfigProperty(String propertyName, String value) {
    configProperties.put(propertyName, value);
  }

  public Object getPrefsProperty(String propertyName) {
    return prefsProperties.get(propertyName);
  }

  public void setPrefsProperty(String propertyName, String value) {
    prefsProperties.put(propertyName, value);
  }

  private void loadPropertyValues(String dirLocation, String filename,
      Properties properties) throws IOException {

    final String fullFilepath = dirLocation + filename;
    final InputStream inputStream = new FileInputStream(new File(fullFilepath));
    properties.load(inputStream);

    inputStream.close();
  }

}
