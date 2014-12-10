package mgh14.search.live.application;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import mgh14.search.live.model.WindowsWallpaperSetter;
import mgh14.search.live.web.BingResourceGetter;
import mgh14.search.live.web.ImageSaver;

/**
 *
 */
public class Application {

  private String ROOT_DIR = "C:\\Users\\mgh14\\Pictures\\screen-temp\\";

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

        int counter = 0;
        while(true) {
          if(resourceUris.size() == 0) {
            System.out.println("No more URL's. Exiting...");
            break;
          }
          if(counter >= resourceUris.size()) {
            System.out.println("Reached list size. Refreshing list...");
            resourceUris = getter.getResources(authToken, searchString, ++pageToGet);
            counter = 0;
          }

          final String resource = resourceUris.get(counter).toString();
          final String filetype = resource.substring(resource.lastIndexOf("."));
          final String filename = ROOT_DIR + "rsrc" + System.currentTimeMillis() + filetype;

          counter++;
          try {
            imageSaver.saveImage(resource, ROOT_DIR, filename);
          } catch (IOException e) {
            continue;
          }

          setter.setDesktopWallpaper(filename);

          try {
            Thread.sleep(10000);
          }
          catch (InterruptedException e) {
            System.out.println("Interrupted");
            e.printStackTrace();
          }
        }
      }
    }).start();

  }

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
