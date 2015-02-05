package mgh14.search.live.application;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import mgh14.search.live.gui.ControlPanel;
import mgh14.search.live.gui.controller.MenuBarController;
import mgh14.search.live.model.ParamNames;
import mgh14.search.live.model.web.resource.getter.BingHtmlResourceUrlGetter;
import mgh14.search.live.model.web.util.ApplicationProperties;
import mgh14.search.live.model.web.util.FileUtils;
import mgh14.search.live.service.CommandExecutor;
import mgh14.search.live.service.resource.cycler.CyclerService;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class HtmlApplication {

  @Configuration
  @ComponentScan("mgh14.search.live")
  static class BeanConfiguration {

    @Bean
    public ConcurrentLinkedQueue<String> resourceQueue() {
      return new ConcurrentLinkedQueue<String>();
    }

    @Bean
    public ExecutorService executorService() {
      return Executors.newFixedThreadPool(10);
    }

    @Bean
    public Preferences preferences() {
      return Preferences.systemNodeForPackage(HtmlApplication.class);
    }

  }

  public static final String APPLICATION_NAME = "Search-Live";

  private static final Logger Log = LoggerFactory.getLogger(HtmlApplication.class);
  private static final String DEFAULT_PROFILE = "DummyResources";

  @Autowired
  private ApplicationProperties applicationProperties;
  @Autowired
  private Preferences preferences;
  @Autowired
  private FileUtils fileUtils;

  private CommandLine line;

  /**
   * arg -query: the search query
   * arg -numResults: the number of results to return for each page
   * arg -sleepTime: the number of seconds for each resource to be viewed
   * arg -springProfiles: comma-separated list (no spaces) of spring
   *      profiles to activate
   * arg -startCycle: forces a cycle start without requiring the 'start'
   *      button to be pushed in the GUI.
   */
  public static void main(String[] args) {
    // parse the command line arguments
    final CommandLine line = CommandLineUtils.parseArgs(
      CommandLineUtils.getHtmlResourceOptions(), args);
    if (line == null) {
      Log.error("Error parsing command line arguments");
      System.exit(-1);
    }

    // set relevant spring profile(s)
    String[] springProfiles = (line.hasOption("springProfiles")) ?
      line.getOptionValue("springProfiles").split(",") :
      new String[]{DEFAULT_PROFILE};
    final AnnotationConfigApplicationContext context =
      setUpApplicationContext(springProfiles);
    final HtmlApplication application = context.getBean(HtmlApplication.class);
    application.setCommandLine(line);
    if (application.springProfileIsEnabled(context, DEFAULT_PROFILE)) {
      application.setProperty("append-file-protocol", "true");
    }

    application.setUpInternals(context);

    // begin executor commands
    final CommandExecutor commandExecutor = context.getBean(CommandExecutor.class);
    commandExecutor.run();
  }

  void setCommandLine(CommandLine line) {
    this.line = line;
  }

  void setProperty(String propName, String propValue) {
    applicationProperties.setConfigProperty(propName, propValue);
  }

  void validateNumResults(int numResults, int maxResults) {
    CommandLineUtils.validateNumResults(numResults, maxResults);
  }

  void validateSecondsToSleep(int secondsToSleep) {
    if (secondsToSleep < 1) {
      throw new IllegalArgumentException("Seconds to sleep must be" +
        "a positive integer");
    }
  }

  void validateCommandLineSecondsToSleep(int secondsToSleep) {
    CommandLineUtils.validateSecondsToSleep(secondsToSleep);
  }

  boolean springProfileIsEnabled(ApplicationContext context, String profile) {
    final List<String> profiles = Arrays.asList(context.
      getEnvironment().getActiveProfiles());

    return profiles.contains(profile);
  }

  static AnnotationConfigApplicationContext setUpApplicationContext(String... springProfiles) {
    final AnnotationConfigApplicationContext context =
      new AnnotationConfigApplicationContext();
    context.getEnvironment().setActiveProfiles(springProfiles);
    context.register(BeanConfiguration.class);
    context.refresh();

    return context;
  }

  private int getNumResults() {
    final int numResults = (line.hasOption("numResults")) ?
      Integer.parseInt(line.getOptionValue("numResults")) :
      Integer.parseInt((String) applicationProperties
        .getConfigProperty("default-num-results"));

    validateNumResults(numResults, Integer.parseInt(
      (String) applicationProperties
        .getConfigProperty("max-num-results")));

    return numResults;
  }

  private void setSecondsToSleep(ApplicationContext context) {
    final int defaultSecondsToSleep = Integer.parseInt(
      (String) applicationProperties.getConfigProperty(
        "default-num-seconds-to-sleep"));
    final int prefSecondsToSleep = preferences.getInt(
      ParamNames.NUM_SECONDS_BETWEEN_CYCLES, -1);

    int secondsToSleep;
    // command line option takes precedence
    if (line.hasOption("sleepTime")) {
      secondsToSleep = Integer.parseInt(line
        .getOptionValue("sleepTime"));
      validateCommandLineSecondsToSleep(secondsToSleep);
      Log.info("Using command line arg for sleep cycle: [{}]",
        secondsToSleep);
    }
    else {
      // pref option takes second precedence behind command line
      secondsToSleep = prefSecondsToSleep;
      Log.debug("Initial seconds to sleep pref value: [{}]",
        secondsToSleep);

      try {
        validateSecondsToSleep(secondsToSleep);
        Log.info("Using pref value of seconds to sleep: [{}]",
          secondsToSleep);
      }
      catch (IllegalArgumentException e) {
        // Now the default value in the config takes precedence
        Log.error("Error validating seconds to sleep: ", e);
        Log.info("Using default value: [{}]", defaultSecondsToSleep);
        secondsToSleep = defaultSecondsToSleep;
      }

      // set new cycle seconds pref value
      if (prefSecondsToSleep != secondsToSleep) {
        preferences.put(ParamNames.NUM_SECONDS_BETWEEN_CYCLES,
          String.valueOf(secondsToSleep));
      }
    }

    context.getBean(CyclerService.class)
      .setSecondsToSleep(secondsToSleep);
  }

  private void ensureAppDataDirExists() {
    final File tempResourceDir = new File(System.getProperty(ParamNames.USER_HOME)
      + File.separator + fileUtils.constructFilepathWithSeparator(
      "AppData", "Local", APPLICATION_NAME));
    if (!Files.exists(tempResourceDir.toPath())) {
      Log.info("Temp directory creation: {}", tempResourceDir.mkdirs());
    }
    setProperty(ParamNames.TEMP_RESOURCES_DIR,
      tempResourceDir.toString() + File.separator);
    fileUtils.setCycledResourcesDir(tempResourceDir + File.separator +
      "cycle-resource-temp" + File.separator);
  }

  private void setInitialResourceSaveDir(ApplicationContext context) {
    final String resourceSaveDirPref = preferences.get(
      ParamNames.RESOURCE_SAVE_DIR, "");
    Log.debug("Initial preferences resource save dir: [{}]",
      resourceSaveDirPref);
    if (resourceSaveDirPref == null || resourceSaveDirPref.isEmpty()) {
      context.getBean(MenuBarController.class)
        .handleNewResourceSaveDir();
    }
  }

  private void setUpInternals(ApplicationContext context) {
    // validate numResults
    final int numResults = getNumResults();

    // validate secondsToSleep
    setSecondsToSleep(context);

    // set production properties (if production profile is enabled)
    if (springProfileIsEnabled(context,
      BingHtmlResourceUrlGetter.PRODUCTION_PROFILE)) {
      setUpBingHtmlResourceUrlGetter(context, "images", numResults);
    }

    // create AppData directory for application miscellaneouses (if
    // not already created)
    ensureAppDataDirExists();

    // set directory to save resources to (if it isn't already set)
    setInitialResourceSaveDir(context);

    // set search string in control panel (if present on command line)
    final ControlPanel controlPanel = context.getBean(ControlPanel.class);
    final String query = line.getOptionValue("query");
    controlPanel.setQueryText((query != null) ? query : "");
  }

  private void setUpBingHtmlResourceUrlGetter(ApplicationContext context,
      String resourceType, int numResults) {

    context.getBean(BingHtmlResourceUrlGetter.class).setResourceType(resourceType);
    context.getBean(BingHtmlResourceUrlGetter.class).setNumResultsToGet(numResults);
  }

}
