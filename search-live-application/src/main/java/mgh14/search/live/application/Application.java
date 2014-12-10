package mgh14.search.live.application;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import mgh14.search.live.model.WindowsWallpaperSetter;
import mgh14.search.live.web.BingResourceGetter;
import mgh14.search.live.web.ImageSaver;

/**
 * Application class for starting the background image cycle
 */
public class Application {

  private String ROOT_DIR = "C:\\Users\\mgh14\\Pictures\\screen-temp\\";

  private void sleep(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    }
    catch (InterruptedException e) {
      System.out.println("Interrupted");
      e.printStackTrace();
    }
  }

  private void startCycle(final String authToken, final String searchString) {
    if(searchString == null || searchString.isEmpty()) {
      System.out.println("Again, please enter a search query (e.g. \"desktop wallpaper\"");
      return;
    }

    // run wallpaper cycle on separate thread
    new Thread(new Runnable() {
      public void run(){

        BingResourceGetter getter = new BingResourceGetter("Image");
        int pageToGet = 1;
        List<URI> resourceUris = getter.getResources(authToken, searchString, pageToGet);

        WindowsWallpaperSetter setter = new WindowsWallpaperSetter();
        ImageSaver imageSaver = new ImageSaver();

        while (true) {
          for (URI resource : resourceUris) {
            final String resourceStr = resource.toString();
            final String filetype = resourceStr.substring(resourceStr.lastIndexOf("."));
            final String filename = ROOT_DIR + "rsrc" + System.currentTimeMillis() + filetype;

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
            sleep(8000);
          }

          // refresh resource URI's
          System.out.println("Reached end of resource list. Refreshing list...");
          resourceUris = getter.getResources(authToken, searchString, ++pageToGet);
        }
      }
    }).start();

  }

  // arg 1: the auth token
  // arg 2: the search query
  public static void main(String[] args) {
    final String authString = args[0];
    if(authString == null || authString.isEmpty()) {
      System.out.println("Please enter your auth token");
      return;
    }
    final String searchString = args[1];
    if(searchString == null || searchString.isEmpty()) {
      System.out.println("Please enter a search query (e.g. \"desktop wallpaper\"");
      return;
    }

    Application application = new Application();
    application.startCycle(authString, searchString);
  }

}
