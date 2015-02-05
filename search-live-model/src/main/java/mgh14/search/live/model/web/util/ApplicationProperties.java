package mgh14.search.live.model.web.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  private Properties configProperties;

  public ApplicationProperties() {
    configProperties = new Properties();
  }

  @PostConstruct
  public void loadConfig() {
    try {
      loadPropertyValues("config" + File.separator +
        "config.properties", configProperties);
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

    final ClassLoader classLoader = getClass().getClassLoader();
    final URL fullFilepath = classLoader.getResource(filename);
    if (fullFilepath == null) {
      Log.error("File not found: {}", filename);
      throw new FileNotFoundException("File not found: " + filename);
    }

    final InputStream inputStream = fullFilepath.openStream();
    properties.load(inputStream);

    inputStream.close();
  }

}
