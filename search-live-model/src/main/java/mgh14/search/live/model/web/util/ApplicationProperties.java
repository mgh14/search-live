package mgh14.search.live.model.web.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Class for loading configuration properties
 */
@Component
public class ApplicationProperties {

  private final Logger Log = LoggerFactory.getLogger(
    getClass().getSimpleName());

  public static final String APPLICATION_NAME = "Dexpec";

  private Properties configProperties;

  public ApplicationProperties() {
    configProperties = new Properties();
  }

  @PostConstruct
  public void loadConfig() {
    try {
      // resources don't use the file separator.
      // Instead they always use a forward slash.
      loadPropertyValues("config/config.properties",
        configProperties);
    }
    catch (IOException e) {
      Log.warn("Warning: Couldn't load properties file. " +
        "Couldn't find config in classpath. Continuing...");
    }
  }

  public void validateNumSleepSeconds(int sleepSeconds) {
    if (sleepSeconds < 1) {
      throw new IllegalArgumentException("Sleep seconds must" +
        "be a positive integer");
    }
  }

  public Object getConfigProperty(String propertyName) {
    return configProperties.get(propertyName);
  }

  public void setConfigProperty(String propertyName, String value) {
    configProperties.put(propertyName, value);
  }

  private void loadPropertyValues(String filename,
      Properties properties) throws IOException {

    final InputStream inputStream = getClass().getClassLoader()
      .getResourceAsStream(filename);
    if (inputStream == null) {
      Log.error("File not found: {}", filename);
      throw new FileNotFoundException("File not found: " + filename);
    }
    properties.load(inputStream);

    inputStream.close();
  }

}
