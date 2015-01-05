package mgh14.search.live.model.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class responsible for loading the resourceQueue that the application
 * will use to set the desktop images
 */
@Component
public class QueueLoader {

  public static final String RESOURCE_FILENAME_PREPEND = "rsrc";
  public static final String RESOURCE_FILENAME_TIMESTAMP_SEPARATOR = "-";

  private final Logger Log = LoggerFactory.getLogger(this.getClass());
  private static final String ROOT_DIR = "C:\\Users\\mgh14\\Pictures\\screen-temp\\";

  @Autowired
  private ResourceUrlGetter resourceUrlGetter;
  @Autowired
  private ConcurrentLinkedQueue<String> resourceQueue;
  @Autowired
  private ExecutorService executorService;
  @Autowired
  private ImageUtils imageUtils;

  private Map<String, String> urlsToFilenames = new HashMap<String, String>();
  private int numPagesToRetrieve = 3;

  private AtomicBoolean downloadsInProgress = new AtomicBoolean(false);

  public void startResourceDownloads() {
    Log.debug("Download resources command invoked. Downloading...");
    downloadsInProgress.set(true);
    executorService.execute(new Runnable() {
      public void run() {
        int downloadCounter = 0;
        List<URI> resourceUris = getShuffledResources(resourceUrlGetter);
        Log.info("Downloading {} resources...", resourceUris.size());
        for (URI resource : resourceUris) {
          // construct (local) filename
          final String resourceStr = resource.toString();
          final String filename = getRelativeResourceFilename(resourceStr, ++downloadCounter);

          // download image
          String finalFilename = downloadResource(resourceStr, filename);
          if(finalFilename != null) {
            urlsToFilenames.put(resourceStr, finalFilename);
          }
        }

        downloadsInProgress.set(false);

        /*// refresh resource URI's if limit reached
        if (resourceUrlGetter.getNumPagesRetrieved() < numPagesToRetrieve) {
          System.out.println("Reached end of resource list for " +
            "page. Refreshing list...");
          startResourceDownloads();
        }
        else {
          Log.info("Finished downloads. Loaded [{}] resource URI's into resource list.",
            urlsToFilenames.size());
        }*/
      }
    });
  }

  public boolean isDownloading() {
    return downloadsInProgress.get();
  }

  private String getRelativeResourceFilename(String resourceStr, int downloadNumber) {
    // construct (local) filename
    final String filetype = resourceStr.substring(resourceStr.lastIndexOf("."));
    return ROOT_DIR + RESOURCE_FILENAME_PREPEND + downloadNumber +
      RESOURCE_FILENAME_TIMESTAMP_SEPARATOR + System.currentTimeMillis() +
      filetype;
  }

  private String downloadResource(String resourceStr, String filename) {
    String finalFilename = null;
    try {
      if (!urlsToFilenames.containsKey(resourceStr)) {
        finalFilename = imageUtils.downloadImage(resourceStr, ROOT_DIR, filename);
        if (!(finalFilename == null || finalFilename.trim().isEmpty())) {
          makeFileReadableAndWriteable(finalFilename);
          resourceQueue.add(finalFilename);
        }
      }
    }
    catch (IOException e) {
      return null;
    }

    return finalFilename;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void makeFileReadableAndWriteable(String filename) {
    final File file = new File(filename);
    file.setReadable(true);
    file.setWritable(true);
  }

  private List<URI> getShuffledResources(ResourceUrlGetter getter) {

    List<URI> resourceUris = getter.getResources();
    Collections.shuffle(resourceUris);

    return resourceUris;
  }

}
