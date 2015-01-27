package mgh14.search.live.gui.menu.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Action associated with clicking 'About' in the
 * File control panel menu
 */
public class AboutAction extends AbstractAction {

  public AboutAction(String text, Icon icon) {
    super(text, icon);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    throw new NotImplementedException();
  }
}
