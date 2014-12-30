package mgh14.search.live.application;

import java.util.concurrent.ConcurrentLinkedQueue;

import mgh14.search.live.model.web.BingHtmlResourceUrlGetter;
import mgh14.search.live.service.CommandExecutor;
import mgh14.search.live.service.messaging.CycleAction;
import mgh14.search.live.service.messaging.CycleCommand;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Application class for starting the background image cycle. This
 * application uses a simple HTTP GET request to fetch resource URL's.
 *
 * The implications of using a simple GET request include no need for
 * an authorization header but also a lack of pagination ability.
 */
@Component
@Configuration
@ComponentScan("mgh14.search.live")
public class HtmlApplication {

  @Bean
  public ConcurrentLinkedQueue<String> resourceQueue() {
    return new ConcurrentLinkedQueue<String>();
  }

  private static final Logger Log = LoggerFactory.getLogger(HtmlApplication.class);

  private ConfigProperties props = new ConfigProperties(
    "C:\\Users\\mgh14\\search-live\\" +
      "search-live-application\\src\\main\\resources\\");

  // arg -query: the search query
  // arg -numResults: the number of results to return for each page
  // arg -sleepTime: the number of seconds for each resource to be viewed
  public static void main(String[] args) {
    final ApplicationContext context =
      new AnnotationConfigApplicationContext(HtmlApplication.class);

    final HtmlApplication application = context.getBean(HtmlApplication.class);
    context.getBean(BingHtmlResourceUrlGetter.class).setResourceType("images");

    // parse the command line arguments
    CommandLine line = application.parseArgs(args);
    if (line == null) {
      Log.error("Error parsing args");
      System.exit(-1);
    }

    // validate numResults
    final int numResults = (line.hasOption("numResults")) ?
      Integer.parseInt(line.getOptionValue("numResults")) :
      Integer.parseInt((String) application.getProperty("default-num-results"));
    application.validateNumResults(numResults,
      Integer.parseInt((String) application.getProperty("max-num-results")));
    context.getBean(BingHtmlResourceUrlGetter.class).setNumResultsToGet(numResults);


    // validate secondsToSleep
    final int secondsToSleep = (line.hasOption("sleepTime")) ?
      Integer.parseInt(line.getOptionValue("sleepTime")) :
      Integer.parseInt((String) application.getProperty("default-num-seconds-to-sleep"));
    application.validateSecondsToSleep(secondsToSleep);

    final CycleCommand startCommand = new CycleCommand(CycleAction.START, "searchString:" +
      line.getOptionValue("query") + ";secondsToSleep:" + secondsToSleep);
    final CommandExecutor commandExecutor = context.getBean(CommandExecutor.class);
    commandExecutor.addCommandToQueue(startCommand);

    // begin executor commands
    commandExecutor.run();
  }

  Object getProperty(String propName) {
    return props.getProperty(propName);
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
