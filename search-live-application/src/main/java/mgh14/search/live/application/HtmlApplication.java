package mgh14.search.live.application;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import mgh14.search.live.gui.ControlPanel;
import mgh14.search.live.model.ParamNames;
import mgh14.search.live.model.web.resource.getter.BingHtmlResourceUrlGetter;
import mgh14.search.live.model.web.util.ApplicationProperties;
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

  private static final Logger Log = LoggerFactory.getLogger(HtmlApplication.class);
  private static final String DEFAULT_PROFILE = "DummyResources";

  @Autowired
  private ApplicationProperties applicationProperties;
  @Autowired
  private Preferences preferences;

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
    if (application.springProfileIsEnabled(context, DEFAULT_PROFILE)) {
      application.setProperty("append-file-protocol", "true");
    }

    // validate numResults
    final int numResults = (line.hasOption("numResults")) ?
      Integer.parseInt(line.getOptionValue("numResults")) :
      Integer.parseInt((String) application.getProperty("default-num-results"));
    application.validateNumResults(numResults,
      Integer.parseInt((String) application.getProperty("max-num-results")));

    // validate secondsToSleep
    final int secondsToSleep = (line.hasOption("sleepTime")) ?
      Integer.parseInt(line.getOptionValue("sleepTime")) :
      Integer.parseInt((String) application.getProperty("default-num-seconds-to-sleep"));
    application.validateSecondsToSleep(secondsToSleep);
    context.getBean(CyclerService.class).setSecondsToSleep(secondsToSleep);

    // set production properties (if production profile is enabled)
    if (application.springProfileIsEnabled(context,
        BingHtmlResourceUrlGetter.PRODUCTION_PROFILE)) {
      application.setUpBingHtmlResourceUrlGetter(context, "images", numResults);
    }

    // set directory to save resources to (if it isn't already set)
    final String resourceSaveDirPref = application.getPreference(
      ParamNames.RESOURCE_SAVE_DIR);
    if (resourceSaveDirPref == null) {
      final String resourceSaveDir = context.getBean(ControlPanel.class)
        .setResourceSaveDirectory();
      if (resourceSaveDir != null) {
        application.putPreference(ParamNames.RESOURCE_SAVE_DIR,
          resourceSaveDir + File.separator);
      }
      else {
        Log.warn("No directory for saving resources has been chosen. " +
          "Save function will not be available.");
      }
    }

    // set search string in control panel (if present on command line)
    final ControlPanel controlPanel = context.getBean(ControlPanel.class);
    final String query = line.getOptionValue("query");
    controlPanel.setQueryText((query != null) ? query : "");

    // begin executor commands
    final CommandExecutor commandExecutor = context.getBean(CommandExecutor.class);
    commandExecutor.run();
  }

  Object getProperty(String propName) {
    return applicationProperties.getConfigProperty(propName);
  }

  void setProperty(String propName, String propValue) {
    applicationProperties.setConfigProperty(propName, propValue);
  }

  String getPreference(String prefName) {
    return preferences.get(prefName, null);
  }

  void putPreference(String prefName, String prefValue) {
    preferences.put(prefName, prefValue);
  }

  void validateNumResults(int numResults, int maxResults) {
    CommandLineUtils.validateNumResults(numResults, maxResults);
  }

  void validateSecondsToSleep(int secondsToSleep) {
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

  private void setUpBingHtmlResourceUrlGetter(ApplicationContext context,
      String resourceType, int numResults) {

    context.getBean(BingHtmlResourceUrlGetter.class).setResourceType(resourceType);
    context.getBean(BingHtmlResourceUrlGetter.class).setNumResultsToGet(numResults);
  }

}
