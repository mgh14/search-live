package mgh14.search.live.gui.controller;

import mgh14.search.live.gui.ControlPanel;
import mgh14.search.live.service.resource.cycler.CyclerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Controller responsible for handling actions associated
 * with the menu bar of the control panel.
 */
@Component
public class MenuBarController {

  @Autowired
  private ControlPanel controlPanel;
  @Autowired
  private CyclerService cyclerService;

  public void handleNewResourceSaveDir() {
    cyclerService.setResourceSaveDir(controlPanel
      .getResourceSaveDirectory());
  }

  public void handleNewCycleSecondsSetting() {
    final int newSecondsToSleep = controlPanel
      .getNewSecondsToSleep();
    cyclerService.setSecondsToSleep(newSecondsToSleep);
  }

}
