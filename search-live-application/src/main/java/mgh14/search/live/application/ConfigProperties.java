package mgh14.search.live.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Class for loading configuration properties
 */
public class ConfigProperties {

  private Properties properties;

  public ConfigProperties(String dirLocation) {
    properties = new Properties();

    try {
      loadPropertyValues(dirLocation);
    }
    catch (IOException e) {
      System.out.println("Warning: Couldn\'t load properties file " +
        "in dir [" + dirLocation + "]. Continuing...");
    }
  }

  public Set<Object> getPropertyKeys() {
    return properties.keySet();
  }

  public Object getProperty(String propertyName) {
    return properties.get(propertyName);
  }

  private void loadPropertyValues(String dirLocation) throws IOException {
    final String fullFilepath = dirLocation + "config.properties";
    final InputStream inputStream = new FileInputStream(new File(fullFilepath));
    properties.load(inputStream);
  }

}
