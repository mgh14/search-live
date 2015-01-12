package mgh14.search.live.application;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class offering helpful methods for parsing command
 * line options.
 */
public class CommandLineUtils {

  private static final Logger Log = LoggerFactory.getLogger(CommandLineUtils.class);

  static CommandLine parseArgs(Options options, String[] args) {
    // create the parser
    CommandLineParser parser = new PosixParser();

    // parse and return the command line arguments
    try {
      return parser.parse(options, args);
    }
    catch (ParseException exp) {
      Log.error("Parsing failed. Reason: {}", exp.getMessage());
      System.exit(-1);
    }

    return null;
  }

  static void validateNumResults(int numResults, int maxResults) {
    // validate numResults
    if (numResults < 1 || numResults > maxResults) {

      System.out.println("Please enter a valid (positive, integer " +
        "between 0 and 50) number of results");
      System.exit(-1);
    }
  }

  static void validateSecondsToSleep(int secondsToSleep) {
    if (secondsToSleep < 0) {
      System.out.println("Please enter a valid (positive, integer) " +
        "number of seconds to sleep");
      System.exit(-1);
    }
  }

  static void validateAuthKey(String authKey) {
    if (authKey == null || authKey.isEmpty()) {
      System.out.println("Please enter your auth key.");
      System.exit(-1);
    }
  }

  static Options getHtmlResourceOptions() {
    Options options = new Options();
    options.addOption(OptionBuilder.hasArg()
      .withDescription("Spring profiles to activate")
      .create("springProfiles"));
    options.addOption(OptionBuilder.isRequired()
      .hasArg()
      .withDescription("Seed resource query (e.g. wallpaper HD)")
      .create("query"));
    options.addOption(OptionBuilder.hasArg()
      .withDescription("Number of results to return for each resource query")
      .create("numResults"));
    options.addOption(OptionBuilder.hasArg()
      .withDescription("Number of seconds to sleep between wallpaper changes")
      .create("sleepTime"));

    return options;
  }

  static Options getApiResourceOptions() {
    final Options options = getHtmlResourceOptions();
    options.addOption(OptionBuilder.isRequired()
      .hasArg()
      .withDescription("Authorization key for access to Bing API")
      .create("authKey"));

    return options;
  }

}
