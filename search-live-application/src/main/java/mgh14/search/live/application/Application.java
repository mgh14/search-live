package mgh14.search.live.application;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import mgh14.search.live.model.WindowsWallpaperSetter;
import mgh14.search.live.web.BingResourceGetter;
import mgh14.search.live.web.ImageSaver;

/**
 * Application class for starting the background image cycle
 */
public class Application {

  private String ROOT_DIR = "C:\\Users\\mgh14\\Pictures\\screen-temp\\";

  // arg 1: the auth token
  // arg 2: the search query
  // arg 3: the number of results to return for each page
  // arg 4: the number of seconds for each resource (NOTE:
  //  for the limit of 5,000 requests/month imposed by
  //  Bing, this should be about 300)
  public static void main(String[] args) {
    if (args.length < 4) {
      System.out.println("Usage: <authString> <searchString (e.g. \"cool wallpaper\")> " +
        "<(int) numResults (> 0, <= 50)> <(int) secondsToSleep (>= 0)>");
    }

    final int numResults = Integer.parseInt(args[2]);
    if (numResults < 0) {
      System.out.println("Please enter a valid (positive, integer) number of results");
      System.exit(-1);
    }
    final int secondsToSleep = Integer.parseInt(args[3]);
    if (numResults < 0) {
      System.out.println("Please enter a valid (positive, integer) number of seconds to sleep");
      System.exit(-1);
    }

    Application application = new Application();
    application.startCycle(args[0], args[1], numResults, secondsToSleep);
  }

  private void startCycle(final String authToken, final String searchString,
      final int numResults, final int secondsToSleep) {

    if(searchString == null || searchString.isEmpty()) {
      System.out.println("Again, please enter a search query (e.g. \"desktop wallpaper\"");
      return;
    }

    // run wallpaper cycle on separate thread
    new Thread(new Runnable() {
      public void run(){

        WindowsWallpaperSetter setter = new WindowsWallpaperSetter();
        ImageSaver imageSaver = new ImageSaver();
        BingResourceGetter getter = new BingResourceGetter("Image", numResults);
        int pageToGet = 1;
        List<URI> resourceUris = getShuffledResources(getter, authToken, searchString, 1);

        while (true) {
          int counter = 0;
          for (URI resource : resourceUris) {
            final String resourceStr = resource.toString();
            final String filetype = resourceStr.substring(resourceStr.lastIndexOf("."));
            final String filename = ROOT_DIR + "rsrc" + counter + "-" +
              System.currentTimeMillis() + filetype;

            // download image
            try {
              imageSaver.saveImage(resourceStr, ROOT_DIR, filename);
            }
            catch (IOException e) {
              continue;
            }

            // set image to desktop
            setter.setDesktopWallpaper(filename);

            // sleep for x milliseconds (enjoy the background!)
            sleep(secondsToSleep * 1000);
          }

          // refresh resource URI's
          System.out.println("Reached end of resource list. Refreshing list...");
          resourceUris = getShuffledResources(getter, authToken, searchString, ++pageToGet);
        }
      }
    }).start();

  }

  private List<URI> getShuffledResources(BingResourceGetter getter, String authToken,
                                         String searchString, int pageToGet) {

    List<URI> resourceUris = getter.getResources(authToken, searchString, pageToGet);
    Collections.shuffle(resourceUris);

    return resourceUris;
  }

  private void sleep(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    }
    catch (InterruptedException e) {
      System.out.println("Interrupted");
      e.printStackTrace();
    }
  }

}
