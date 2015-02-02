package mgh14.search.live.gui.menu;

import javax.annotation.PostConstruct;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.border.Border;

import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import mgh14.search.live.gui.GuiUtils;
import mgh14.search.live.gui.controller.MenuBarController;
import mgh14.search.live.gui.menu.action.AboutAction;
import mgh14.search.live.gui.menu.action.ChooseSaveDirAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Class for managing all things menu bar related
 * in the control panel GUI.
 */
@Component
public class MenuBarManager {

  private final Logger Log = LoggerFactory.getLogger(
    getClass().getSimpleName());

  private static final Border MENU_ITEM_BORDER =
    BorderFactory.createEmptyBorder(1, 7, 1, 7);

  @Autowired
  private ApplicationContext context;
  @Autowired
  private GuiUtils guiUtils;

  private JMenuBar menuBar;
  private JMenu fileMenu;
  private JMenu settingsMenu;

  public MenuBarManager() {
    menuBar = new JMenuBar();
    fileMenu = new JMenu("File");
    settingsMenu = new JMenu("Settings");

    setupMenuBar();
  }

  @PostConstruct
  private void createFileMenuItems() {
    JMenuItem menuItem = new JMenuItem(
      new AboutAction(
        getMenuItemFormattedText("About"),
        guiUtils.getImageIcon("save-small.png")));

    menuItem.setBorder(MENU_ITEM_BORDER);
    fileMenu.add(menuItem);

    fileMenu.setVisible(true);
  }

  @PostConstruct
  private void createSettingsMenuItems() {
    // resource save directory option
    final MenuBarController menuBarController = context
      .getBean(MenuBarController.class);
    JMenuItem menuItem = new JMenuItem(
      new ChooseSaveDirAction(
        getMenuItemFormattedText("Set save directory"),
        guiUtils.getImageIcon("save-small.png"),
        menuBarController));

    menuItem.setBorder(MENU_ITEM_BORDER);
    //menuItem.setMnemonic(KeyEvent.VK_A);
    settingsMenu.add(menuItem);

    settingsMenu.setVisible(true);
  }

  public void addMenuBarToFrame(JFrame frame) {
    Log.debug("Adding menu bar to frame [{}]...", frame);
    frame.setJMenuBar(menuBar);
    frame.revalidate();
  }

  private void setupMenuBar() {
    Log.debug("Setting up the menu bar...");
    menuBar.putClientProperty(Options.HEADER_STYLE_KEY,
      HeaderStyle.SINGLE);
    menuBar.add(fileMenu);
    menuBar.add(settingsMenu);
  }

  private String getMenuItemFormattedText(String text) {
    return "<html><p style=\"margin:0px; margin-left:5px;\">" +
      text + "</p></html>";
  }
}
