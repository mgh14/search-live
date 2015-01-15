package mgh14.search.live.model.web.resource.getter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;

import mgh14.search.live.model.web.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Dummy class created so as not to have to download
 * images from the network whenever developing functionality.
 */
@Component
@Profile("DummyResources")
public class DummyResourceUrlGetter implements ResourceUrlGetter {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  @Autowired
  private FileUtils fileUtils;

  private String dummyResourceDir = null;
  private List<String> dummyResourceUris;

  public DummyResourceUrlGetter() {
    dummyResourceUris = new LinkedList<String>();
  }

  @PostConstruct
  public void setDummyResourceDirectory() {
    dummyResourceDir = fileUtils.constructFilepathWithSeparator(
      "C:", "Users", "mgh14", "Pictures", "dummy-resources");
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
    final File[] folderFiles = new File(dummyResourceDir).listFiles();
    if(folderFiles != null) {
      for (final File fileEntry : folderFiles) {
        dummyResourceUris.add(fileEntry.getAbsolutePath());
      }
    }
  }
}
