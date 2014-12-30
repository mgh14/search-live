package mgh14.search.live.application;

import mgh14.search.live.gui.ControlPanel;
import mgh14.search.live.model.messaging.CycleAction;
import mgh14.search.live.model.messaging.CycleCommand;
import mgh14.search.live.model.web.BingHtmlResourceUrlGetter;
import mgh14.search.live.service.CommandExecutor;
import mgh14.search.live.service.ResourceCycler;
import mgh14.search.live.service.SaveController;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application class for starting the background image cycle. This
 * application uses a simple HTTP GET request to fetch resource URL's.
 *
 * The implications of using a simple GET request include no need for
 * an authorization header but also a lack of pagination ability.
 */
public class HtmlApplication {

  private static final Logger Log = LoggerFactory.getLogger(HtmlApplication.class);

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
      Log.error("Error parsing args");
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

    // instantiate application objects
    final SaveController controller = new SaveController();
    new ControlPanel(controller);

    final CommandExecutor executor = new CommandExecutor();
    executor.setResourceCycler(new ResourceCycler(
      new BingHtmlResourceUrlGetter("images", numResults)));
    controller.setCommandExecutor(executor);

    final CycleCommand startCommand = new CycleCommand(CycleAction.START, "searchString:" +
      line.getOptionValue("query") + ";secondsToSleep:" + secondsToSleep);
    executor.addCommandToQueue(startCommand);

    // begin executor commands
    executor.run();
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
