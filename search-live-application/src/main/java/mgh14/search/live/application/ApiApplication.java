package mgh14.search.live.application;

import mgh14.search.live.web.BingApiResourceUrlGetter;
import org.apache.commons.cli.CommandLine;

/**
 * Application class for starting the background image cycle. This
 * application uses Microsoft's Azure Marketplace API to fetch the
 * resource URL's.
 *
 * The implications of using the API here include the need for
 * an authorization header but also the ability to paginate.
 */
public class ApiApplication {

  static ConfigProperties props;
  static {
    props = new ConfigProperties("C:\\Users\\mgh14\\search-live\\" +
      "search-live-application\\src\\main\\resources\\");
  }

  // arg -authKey: the Bing API access key
  // arg -query: the search query
  // arg -numResults: the number of results to return for each page
  // arg -sleepTime: the number of seconds for each resource to
  // be viewed (NOTE: for the limit of 5,000 requests/month imposed
  // by Bing, this argument should be about 300)
  public static void main(String[] args) {
    final ApiApplication application = new ApiApplication();

    // parse the command line arguments
    CommandLine line = application.parseArgs(args);
    if (line == null) {
      System.out.println("Error parsing args");
      System.exit(-1);
    }

    final String authKey = (line.hasOption("authKey")) ?
      line.getOptionValue("authKey") : null;
    application.validateAuthKey(authKey);

    // validate numResults
    final int numResults = (line.hasOption("numResults")) ?
      Integer.parseInt(line.getOptionValue("numResults")) :
      Integer.parseInt((String) props.getProperty("default-num-results"));
    application.validateNumResults(numResults,
      Integer.parseInt((String) props.getProperty("max-num-results")));

    // validate secondsToSleep
    final int secondsToSleep = (line.hasOption("sleepTime")) ?
      Integer.parseInt(line.getOptionValue("sleepTime")) :
      Integer.parseInt((String) props.getProperty("default-num-seconds-to-sleep"));
    application.validateSecondsToSleep(secondsToSleep);

    ApplicationCycler htmlApplication = new ApplicationCycler(
      new BingApiResourceUrlGetter(authKey, "Image", numResults));
    htmlApplication.startCycle(line.getOptionValue("query"), secondsToSleep);
  }

  CommandLine parseArgs(String[] args) {
    return CommandLineHelper.parseArgs(CommandLineHelper.getApiResourceOptions(), args);
  }

  void validateAuthKey(String authKey) {
    CommandLineHelper.validateAuthKey(authKey);
  }

  void validateNumResults(int numResults, int maxResults) {
    CommandLineHelper.validateNumResults(numResults, maxResults);
  }

  void validateSecondsToSleep(int secondsToSleep) {
    CommandLineHelper.validateSecondsToSleep(secondsToSleep);
  }
}
