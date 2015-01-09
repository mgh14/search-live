package mgh14.search.live.model.wallpaper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import mgh14.search.live.model.web.resource.getter.ResourceUrlGetter;
import mgh14.search.live.model.web.util.ImageUtils;
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
  private static final int NUM_DOWNLOADS_PER_REQUEST = 5;

  @Autowired
  private ResourceUrlGetter resourceUrlGetter;
  @Autowired
  private ConcurrentLinkedQueue<String> resourceQueue;
  @Autowired
  private ExecutorService executorService;
  @Autowired
  private ImageUtils imageUtils;

  private Map<String, String> urlsToFilenames = new HashMap<String, String>();
  private Queue<String> currentResourceLocations = new ConcurrentLinkedQueue<String>();
  private int numPagesToRetrieve = 3;
  private AtomicInteger downloadCounter = new AtomicInteger(0);

  private AtomicBoolean downloadsInProgress = new AtomicBoolean(false);

  public void startResourceDownloads() {
    Log.debug("Download resources cycle invoked. Downloading...");

    downloadsInProgress.set(true);
    executorService.execute(new Runnable() {
      public void run() {
        List<String> resourceUris = getSetOfResourceLocations();

        Log.info("Downloading {} resources...", resourceUris.size());
        for (String resource : resourceUris) {
          // construct (local) filename
          final String filename = getRelativeResourceFilename(resource);

          // download image
          String finalFilename = downloadResource("file:///" + resource, filename);
          if(finalFilename != null) {
            urlsToFilenames.put(resource, finalFilename);
          }
        }

        downloadsInProgress.set(false);
      }
    });
  }

  public boolean isDownloading() {
    return downloadsInProgress.get();
  }

  private List<String> getSetOfResourceLocations() {
    final List<String> setOfResourceLocations = new LinkedList<String>();
    for (int i=0; i<NUM_DOWNLOADS_PER_REQUEST; i++) {
      if (currentResourceLocations.isEmpty()) {
        Log.debug("Queued resources from getter class is empty. " +
          "Retrieving next page of resources...");
        currentResourceLocations = getShuffledResources(resourceUrlGetter);
        if (currentResourceLocations.isEmpty()) {
          Log.debug("Getter has no more resources to retrieve.");
          // There are no more results to retrieve
          // from the network. Return the list with any
          // remaining items and don't add any more.
          return setOfResourceLocations;
        }
      }

      setOfResourceLocations.add(currentResourceLocations.poll());
    }

    return setOfResourceLocations;
  }

  private String getRelativeResourceFilename(String resourceStr) {
    // construct (local) filename
    final String filetype = resourceStr.substring(resourceStr.lastIndexOf("."));
    return ROOT_DIR + RESOURCE_FILENAME_PREPEND + downloadCounter.incrementAndGet() +
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

  private Queue<String> getShuffledResources(ResourceUrlGetter getter) {
    List<String> resourceUris = getter.getResources();
    Collections.shuffle(resourceUris);

    final Queue<String> resourceUrisForQueue = new ConcurrentLinkedQueue<String>();
    for (String resourceUri : resourceUris) {
      resourceUrisForQueue.add(resourceUri);
    }
    return resourceUrisForQueue;
  }

}
