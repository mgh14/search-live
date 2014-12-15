package mgh14.search.live.application;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import mgh14.search.live.model.WindowsWallpaperSetter;
import mgh14.search.live.web.ImageSaver;
import mgh14.search.live.web.ResourceUrlGetter;

/**
 *
 */
public class ApplicationCycler {

  private static final String ROOT_DIR = "C:\\Users\\mgh14\\Pictures\\screen-temp\\";

  private ResourceUrlGetter resourceUrlGetter;

  public ApplicationCycler(ResourceUrlGetter resourceUrlGetter) {
    this.resourceUrlGetter = resourceUrlGetter;
  }

  public void startCycle(final String searchString, final int secondsToSleep) {
    if(searchString == null || searchString.isEmpty()) {
      System.out.println("Please enter a search query (e.g. \"desktop wallpaper\"");
      return;
    }
    resourceUrlGetter.setSearchString(searchString);

    // run wallpaper cycle on separate thread
    new Thread(new Runnable() {
      public void run(){

        WindowsWallpaperSetter setter = new WindowsWallpaperSetter();
        ImageSaver imageSaver = new ImageSaver();
        List<URI> resourceUris = getShuffledResources(resourceUrlGetter);

        while (true) {
          int counter = 0;
          for (URI resource : resourceUris) {
            final String resourceStr = resource.toString();
            final String filetype = resourceStr.substring(resourceStr.lastIndexOf("."));
            final String filename = ROOT_DIR + "rsrc" + counter++ + "-" +
              System.currentTimeMillis() + filetype;

            // download image
            String finalFilename;
            try {
              finalFilename = imageSaver.saveImage(resourceStr, ROOT_DIR, filename);
            }
            catch (IOException e) {
              continue;
            }

            // set image to desktop
            setter.setDesktopWallpaper(finalFilename);

            // sleep for x milliseconds (enjoy the background!)
            sleep(secondsToSleep * 1000);
          }

          // refresh resource URI's
          System.out.println("Reached end of resource list. Refreshing list...");
          resourceUris = getShuffledResources(resourceUrlGetter);
        }
      }
    }).start();

  }

  private List<URI> getShuffledResources(ResourceUrlGetter getter) {

    List<URI> resourceUris = getter.getResources();
    Collections.shuffle(resourceUris);

    return resourceUris;
  }

  private void sleep(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    }
    catch (InterruptedException e) {
      System.out.println("Interrupted cycle");
      e.printStackTrace();
    }
  }

}
