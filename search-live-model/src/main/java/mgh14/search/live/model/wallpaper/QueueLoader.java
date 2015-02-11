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
import mgh14.search.live.model.web.util.ApplicationProperties;
import mgh14.search.live.model.web.util.FileUtils;
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

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());
  private static final int NUM_DOWNLOADS_PER_REQUEST = 5;

  @Autowired
  private ApplicationProperties applicationProperties;
  @Autowired
  private ResourceUrlGetter resourceUrlGetter;
  @Autowired
  private ConcurrentLinkedQueue<String> resourceQueue;
  @Autowired
  private ExecutorService executorService;
  @Autowired
  private ImageUtils imageUtils;
  @Autowired
  private FileUtils fileUtils;
  @Autowired
  private ExpiredResourcesDeleter expiredResourcesDeleter;

  private Map<String, String> urlsToFilenames = new HashMap<String, String>();
  private Queue<String> currentResourceLocations = new ConcurrentLinkedQueue<String>();
  private AtomicInteger downloadCounter = new AtomicInteger(0);
  private AtomicBoolean downloadsInProgress = new AtomicBoolean(false);

  public void startResourceDownloads() {
    if (downloadsInProgress.get()) {
      return;
    }

    // delete expired resources
    expiredResourcesDeleter.deleteExpiredFiles(new File(
      fileUtils.getResourceDir()));

    downloadsInProgress.set(true);
    Log.debug("Download resources cycle invoked. Starting download thread...");

    executorService.execute(new Runnable() {
      public void run() {
        Log.debug("Downloading on thread {}.", Thread.currentThread().getName());
        List<String> resourceUris = getSetOfResourceLocations();
        if (resourceUris.isEmpty()) {
          Log.info("Resource URI's is empty. Terminating download thread {}...",
            Thread.currentThread().getName());
          downloadsInProgress.set(false);
          return;   // terminate thread
        }

        Log.info("Downloading {} resources...", resourceUris.size());
        for (String resource : resourceUris) {
          // construct (local) filename
          final String filename = fileUtils.getRelativeResourceFilename(
            resource, downloadCounter.incrementAndGet());

          // download image
          if ("true".equals(applicationProperties
              .getConfigProperty("append-file-protocol"))) {
            resource = "file:///" + resource;
          }
          String finalFilename = downloadResource(resource, filename);
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

  public void resetQueueLoader() {
    downloadsInProgress.set(false);

    Log.debug("Emptying resource queue...");
    resourceQueue.clear();

    currentResourceLocations.clear();
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

  private String downloadResource(String resourceStr, String filename) {
    String finalFilename = null;
    try {
      if (!urlsToFilenames.containsKey(resourceStr)) {
        finalFilename = imageUtils.downloadImage(resourceStr,
          fileUtils.getResourceDir(), filename);
        if (!(finalFilename == null || finalFilename.trim().isEmpty())) {
          fileUtils.makeFileReadableAndWriteable(finalFilename);
          resourceQueue.add(finalFilename);
        }
      }
    }
    catch (IOException e) {
      return null;
    }

    return finalFilename;
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
