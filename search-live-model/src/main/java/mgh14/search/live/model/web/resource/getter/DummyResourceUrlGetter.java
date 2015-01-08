package mgh14.search.live.model.web.resource.getter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Dummy class created so as not to have to download
 * images from the network whenever developing functionality.
 */
@Component
public class DummyResourceUrlGetter implements ResourceUrlGetter {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  private static final File DUMMY_RESOURCE_DIR = new File("C:\\Users\\mgh14\\Pictures\\dummy-resources\\");

  private List<String> dummyResourceUris;

  public DummyResourceUrlGetter() {
    dummyResourceUris = new LinkedList<String>();
    loadDummyResources();
  }

  @Override
  public void setSearchString(String searchString) {}

  @Override
  public List<String> getResources() {
    Log.info("Getting dummy resources...");
    return dummyResourceUris;
  }

  @Override
  public int getNumPagesRetrieved() {
    throw new NotImplementedException();
  }

  private void loadDummyResources() {
    final File[] folderFiles = DUMMY_RESOURCE_DIR.listFiles();
    if(folderFiles != null) {
      for (final File fileEntry : folderFiles) {
        dummyResourceUris.add(fileEntry.getAbsolutePath());
      }
    }
  }
}
