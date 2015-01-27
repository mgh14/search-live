package mgh14.search.live.gui;

import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import mgh14.search.live.model.web.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class providing utility GUI methods for GUI-
 * related functionality.
 */
@Component
public class GuiUtils {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  private static final String ICONS_LOCATION = "icons" + File.separator;

  @Autowired
  private FileUtils fileUtils;

  private final ClassLoader classLoader;

  public GuiUtils() {
    classLoader = getClass().getClassLoader();
    if (classLoader == null) {
      throw new IllegalStateException("Cannot find classloader!");
    }
  }

  public ImageIcon getImageIcon(String iconFilename) {
    Log.debug("Locating icon [{}]...", iconFilename);
    final URL iconUrl = classLoader.getResource(
      ICONS_LOCATION + iconFilename);
    return (iconUrl == null) ? null : new ImageIcon(
      iconUrl);
  }

  public String chooseFileLocation(JFrame mainFrame) {
    final JFileChooser fileChooser = new JFileChooser(
      fileUtils.getResourceDir());
    fileChooser.setDialogTitle("Choose the directory for saved images");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    int returnValue = fileChooser.showOpenDialog(mainFrame);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile().getAbsolutePath() +
        File.separator;
    }

    return null;
  }

}