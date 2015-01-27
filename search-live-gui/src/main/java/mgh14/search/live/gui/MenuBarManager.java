package mgh14.search.live.gui;

import javax.annotation.PostConstruct;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for managing all things menu bar related
 * in the control panel GUI.
 */
@Component
public class MenuBarManager {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  @Autowired
  private GuiUtils guiUtils;

  private JMenuBar menuBar;
  private JMenu fileMenu;

  public MenuBarManager() {
    menuBar = new JMenuBar();
    fileMenu = new JMenu("File");

    setupMenuBar();
  }

  @PostConstruct
  private void createFileMenuItems() {
    JMenuItem menuItem = new JMenuItem("Set resource save " +
      "location", guiUtils.getImageIcon("save.png"));
    //menuItem.setMnemonic(KeyEvent.VK_A);
    menuItem.getAccessibleContext().setAccessibleDescription(
      "This doesn't really do anything");
    fileMenu.add(menuItem);

    menuItem = new JMenuItem("About",
      guiUtils.getImageIcon("save.png"));
    fileMenu.add(menuItem);

    fileMenu.setVisible(true);
  }

  public void addMenuBarToFrame(JFrame frame) {
    Log.debug("Adding menu bar to frame [{}]...", frame);
    frame.setJMenuBar(menuBar);
    frame.revalidate();
  }

  private void setupMenuBar() {
    menuBar.putClientProperty(Options.HEADER_STYLE_KEY,
      HeaderStyle.SINGLE);
    menuBar.add(fileMenu);
  }

}
