package mgh14.search.live.gui.menu.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;

import mgh14.search.live.gui.controller.MenuBarController;

/**
 * Action associated with setting the save directory in the
 * Settings control panel menu
 */
public class ChooseSaveDirAction extends AbstractAction {

  private MenuBarController menuBarController;

  public ChooseSaveDirAction(String text, Icon icon,
      MenuBarController menuBarController) {

    super(text, icon);
    this.menuBarController = menuBarController;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    menuBarController.handleNewResourceSaveDir();
  }
}
