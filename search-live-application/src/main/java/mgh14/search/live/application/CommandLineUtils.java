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

  private static final Logger Log = LoggerFactory.getLogger(
    CommandLineUtils.class);

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

      System.out.println("Please enter a valid (positive, " +
        "integer (between 0 and 50) number of results");
      System.exit(-1);
    }
  }

  static void validateSecondsToSleep(int secondsToSleep) {
    if (secondsToSleep < 1) {
      System.out.println("Please enter a valid (positive, " +
        "integer) number of seconds to sleep");
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

    OptionBuilder.hasArg();
    OptionBuilder.withDescription("Spring profiles to activate");
    options.addOption(OptionBuilder.create("springProfiles"));

    OptionBuilder.isRequired();
    OptionBuilder.hasArg();
    OptionBuilder.withDescription("Seed resource query (e.g. " +
      "wallpaper HD)");
    options.addOption(OptionBuilder.create("query"));

    OptionBuilder.hasArg();
    OptionBuilder.withDescription("Number of seconds to sleep " +
      "between wallpaper changes");
    options.addOption(OptionBuilder.create("numResults"));

    OptionBuilder.hasArg();
    OptionBuilder.withDescription("Number of seconds to sleep " +
      "between wallpaper changes");
    options.addOption(OptionBuilder.create("sleepTime"));

    OptionBuilder.hasArg(false);
    OptionBuilder.withDescription("Whether or not to immediately " +
      "start the resource cycle. " +
      "Requires that the -query argument is set.");
    options.addOption(OptionBuilder.create("startCycle"));

    return options;
  }

  static Options getApiResourceOptions() {
    final Options options = getHtmlResourceOptions();

    OptionBuilder.hasArg();
    OptionBuilder.withDescription("Authorization key for access to " +
      "Bing API");
    options.addOption(OptionBuilder.create("authKey"));

    return options;
  }

}
