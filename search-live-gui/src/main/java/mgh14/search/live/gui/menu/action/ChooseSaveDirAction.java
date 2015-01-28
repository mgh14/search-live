package mgh14.search.live.gui.menu.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;

import mgh14.search.live.gui.controller.GuiController;

/**
 * Action associated with setting the save directory in the
 * Settings control panel menu
 */
public class ChooseSaveDirAction extends AbstractAction {

  private GuiController guiController;

  public ChooseSaveDirAction(String text, Icon icon,
      GuiController guiController) {

    super(text, icon);
    this.guiController = guiController;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    guiController.handleNewResourceSaveDir();
  }
}
