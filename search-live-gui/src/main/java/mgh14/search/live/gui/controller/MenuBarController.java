package mgh14.search.live.gui.controller;

import mgh14.search.live.gui.ControlPanel;
import mgh14.search.live.model.web.util.ApplicationProperties;
import mgh14.search.live.service.resource.cycler.CyclerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Controller responsible for handling actions associated
 * with the menu bar of the control panel.
 */
@Component
public class MenuBarController {

  private final Logger Log = LoggerFactory.getLogger(
    getClass().getSimpleName());

  @Autowired
  private ApplicationProperties applicationProperties;
  @Autowired
  private ControlPanel controlPanel;
  @Autowired
  private CyclerService cyclerService;

  public int getNumSecondsToSleep() {
    return cyclerService.getSecondsToSleep();
  }

  public void handleNewResourceSaveDir() {
    cyclerService.setResourceSaveDir(controlPanel
      .getResourceSaveDirectory());
  }

  public void handleNewCycleSecondsSetting() {
    final int newSecondsToSleep = controlPanel
      .getNewSecondsToSleep();

    try {
      applicationProperties.validateNumSleepSeconds(
        newSecondsToSleep);
      cyclerService.setSecondsToSleep(newSecondsToSleep,
        true);
    } catch (IllegalArgumentException e) {
      Log.error("Error setting sleep seconds: ", e);
    }

  }

}
