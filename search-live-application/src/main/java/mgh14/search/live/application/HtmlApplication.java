package mgh14.search.live.application;

import mgh14.search.live.gui.SaveGui;
import mgh14.search.live.model.web.BingHtmlResourceUrlGetter;
import mgh14.search.live.service.ApplicationCycler;
import mgh14.search.live.service.SaveController;
import org.apache.commons.cli.CommandLine;

/**
 * Application class for starting the background image cycle. This
 * application uses a simple HTTP GET request to fetch resource URL's.
 *
 * The implications of using a simple GET request include no need for
 * an authorization header but also a lack of pagination ability.
 */
public class HtmlApplication {

  static ConfigProperties props;
  static {
    props = new ConfigProperties("C:\\Users\\mgh14\\search-live\\" +
      "search-live-application\\src\\main\\resources\\");
  }

  // arg -query: the search query
  // arg -numResults: the number of results to return for each page
  // arg -sleepTime: the number of seconds for each resource to be viewed
  public static void main(String[] args) {
    final HtmlApplication application = new HtmlApplication();

    // parse the command line arguments
    CommandLine line = application.parseArgs(args);
    if (line == null) {
      System.out.println("Error parsing args");
      System.exit(-1);
    }

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

    SaveController controller = new SaveController();
    new SaveGui(controller);
    ApplicationCycler htmlApplication = new ApplicationCycler(
      new BingHtmlResourceUrlGetter("images", numResults));
    controller.setApplicationCycler(htmlApplication);
    htmlApplication.startCycle(line.getOptionValue("query"), secondsToSleep);
  }

  CommandLine parseArgs(String[] args) {
    return CommandLineHelper.parseArgs(CommandLineHelper.getHtmlResourceOptions(), args);
  }

  void validateNumResults(int numResults, int maxResults) {
    CommandLineHelper.validateNumResults(numResults, maxResults);
  }

  void validateSecondsToSleep(int secondsToSleep) {
    CommandLineHelper.validateSecondsToSleep(secondsToSleep);
  }

}
