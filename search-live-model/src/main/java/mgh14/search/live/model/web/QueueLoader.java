package mgh14.search.live.model.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
  private Queue<URI> currentResourceUris = new ConcurrentLinkedQueue<URI>();
  private int numPagesToRetrieve = 3;
  private int downloadCounter = 0;

  private AtomicBoolean downloadsInProgress = new AtomicBoolean(false);

  public void startResourceDownloads() {
    Log.debug("Download resources cycle invoked. Downloading...");

    downloadsInProgress.set(true);
    executorService.execute(new Runnable() {
      public void run() {
        List<URI> resourceUris = getSetOfResourceUris();

        Log.info("Downloading {} resources...", resourceUris.size());
        for (URI resource : resourceUris) {
          // construct (local) filename
          final String resourceStr = resource.toString();
          final String filename = getRelativeResourceFilename(resourceStr);

          // download image
          String finalFilename = downloadResource(resourceStr, filename);
          if(finalFilename != null) {
            urlsToFilenames.put(resourceStr, finalFilename);
          }
        }

        downloadsInProgress.set(false);
      }
    });
  }

  public boolean isDownloading() {
    return downloadsInProgress.get();
  }

  private List<URI> getSetOfResourceUris() {
    final List<URI> setOfResourceUris = new LinkedList<URI>();
    for (int i=0; i<NUM_DOWNLOADS_PER_REQUEST; i++) {
      if (currentResourceUris.isEmpty()) {
        Log.debug("Queued resources from getter class is empty. " +
          "Retrieving next page of resources...");
        currentResourceUris = getShuffledResources(resourceUrlGetter);
        if (currentResourceUris.isEmpty()) {
          Log.debug("Getter has no more resources to retrieve.");
          // There are no more results to retrieve
          // from the network. Return the list with any
          // remaining items and don't add any more.
          return setOfResourceUris;
        }
      }

      setOfResourceUris.add(currentResourceUris.poll());
    }

    return setOfResourceUris;
  }

  private String getRelativeResourceFilename(String resourceStr) {
    // construct (local) filename
    final String filetype = resourceStr.substring(resourceStr.lastIndexOf("."));
    return ROOT_DIR + RESOURCE_FILENAME_PREPEND + ++downloadCounter +
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

  private Queue<URI> getShuffledResources(ResourceUrlGetter getter) {
    List<URI> resourceUris = getter.getResources();
    Collections.shuffle(resourceUris);

    final Queue<URI> resourceUrisForQueue = new ConcurrentLinkedQueue<URI>();
    for (URI resourceUri : resourceUris) {
      resourceUrisForQueue.add(resourceUri);
    }
    return resourceUrisForQueue;
  }

}
