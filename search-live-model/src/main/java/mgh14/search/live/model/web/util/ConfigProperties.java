package mgh14.search.live.model.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for loading configuration properties
 */
public class ConfigProperties {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  private Properties properties;

  public ConfigProperties() {
    properties = new Properties();
  }

  public void setConfigFileLocation(String dirLocation) {
    try {
      loadPropertyValues(dirLocation);
    }
    catch (IOException e) {
      Log.warn("Warning: Couldn\'t load properties file " +
        "in dir [{}]. Continuing...", dirLocation);
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
