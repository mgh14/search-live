package mgh14.search.live.gui.controller;

import mgh14.search.live.gui.ControlPanel;
import mgh14.search.live.service.resource.cycler.CyclerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by mgh14 on 2/2/2015.
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

}
