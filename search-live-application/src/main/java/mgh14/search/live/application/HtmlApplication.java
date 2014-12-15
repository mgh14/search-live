package mgh14.search.live.application;

import mgh14.search.live.web.BingHtmlResourceUrlGetter;

/**
 * Application class for starting the background image cycle. This
 * application uses a simple HTTP GET request to fetch resource URL's.
 *
 * The implications of using a simple GET request include no need for
 * an authorization header but also a lack of pagination ability.
 */
public class HtmlApplication {

  // arg 1: the search query
  // arg 2: the number of results to return for each page
  // arg 3: the number of seconds for each resource
  public static void main(String[] args) {
    if (args.length < 3) {
      System.out.println("Usage: <searchString (e.g. \"cool wallpaper\")> " +
        "<(int) numResults (> 0, <= 50)> <(int) secondsToSleep (>= 0)>");
    }

    final int numResults = Integer.parseInt(args[1]);
    if (numResults < 0) {
      System.out.println("Please enter a valid (positive, integer) number of results");
      System.exit(-1);
    }
    final int secondsToSleep = Integer.parseInt(args[2]);
    if (numResults < 0) {
      System.out.println("Please enter a valid (positive, integer) number of seconds to sleep");
      System.exit(-1);
    }

    ApplicationCycler htmlApplication = new ApplicationCycler(new BingHtmlResourceUrlGetter("images"));
    htmlApplication.startCycle(args[0], secondsToSleep);
  }

}
