package mgh14.search.live.web;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
public class QueueLoader {

  private static final String ROOT_DIR = "C:\\Users\\mgh14\\Pictures\\screen-temp\\";

  private Map<String, String> urlsToFilenames = new HashMap<String, String>();
  private ConcurrentLinkedQueue<String> queue;

  public void setQueue(ConcurrentLinkedQueue<String> queue) {
    this.queue = queue;
  }

  public String getRelativeResourceFilename(String resourceStr, int downloadNumber) {
    // construct (local) filename
    final String filetype = resourceStr.substring(resourceStr.lastIndexOf("."));
    return ROOT_DIR + "rsrc" + downloadNumber + "-" +
      System.currentTimeMillis() + filetype;
  }

  public void startResourceDownloads(final ResourceUrlGetter resourceUrlGetter) {
    new Thread(new Runnable() {
      public void run() {
        ImageSaver imageSaver = new ImageSaver();

        int downloadCounter = 0;
        List<URI> resourceUris = getShuffledResources(resourceUrlGetter);
        for (URI resource : resourceUris) {
          // construct (local) filename
          final String resourceStr = resource.toString();
          final String filename = getRelativeResourceFilename(resourceStr, ++downloadCounter);

          // download image
          String finalFilename = null;
          try {
            if (!urlsToFilenames.containsKey(resourceStr)) {
              finalFilename = imageSaver.saveImage(resourceStr, ROOT_DIR, filename);
              if (!(finalFilename == null || finalFilename.trim().isEmpty())) {
                queue.add(finalFilename);
              }
            }
          }
          catch (IOException e) {
            continue;
          }

          if(finalFilename != null)
            urlsToFilenames.put(resourceStr, finalFilename);
        }

        // refresh resource URI's if limit reached
      /*if (++counter >= numResults) {
        System.out.println("Reached end of resource list. Refreshing list...");
        queueLoader.startResourceDownloads(resourceUrlGetter);
        counter = 0;
      }*/

        System.out.println("Finished downloads");
      }
    }).start();
  }

  private List<URI> getShuffledResources(ResourceUrlGetter getter) {

    List<URI> resourceUris = getter.getResources();
    Collections.shuffle(resourceUris);

    return resourceUris;
  }

}
