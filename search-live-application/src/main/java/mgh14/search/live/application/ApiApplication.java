package mgh14.search.live.application;

import mgh14.search.live.web.BingApiResourceUrlGetter;

/**
 *
 */
public class ApiApplication {

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

    ApplicationCycler htmlApplication = new ApplicationCycler(new BingApiResourceUrlGetter(args[0], "Image", numResults));
    htmlApplication.startCycle(args[1], secondsToSleep);
  }
}
