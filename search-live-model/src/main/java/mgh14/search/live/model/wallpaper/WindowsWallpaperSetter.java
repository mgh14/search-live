package mgh14.search.live.model.wallpaper;

import java.io.File;
import java.io.IOException;

import mgh14.search.live.model.web.util.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for setting desktop wallpaper given the path to an image
 * for the Windows operating system.
 */
// TODO: Test in Windows environments other than Windows 7 64-bit
@Component
public class WindowsWallpaperSetter {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  @Autowired
  private ApplicationProperties applicationProperties;

  public void setDesktopWallpaper(String path) {
    if (path == null || path.isEmpty()) {
      Log.error("Invalid null/empty file name");
      return;
    }

    try {
      Runtime.getRuntime().exec(
        applicationProperties.getConfigProperty("installation-dir") +
        "bin" + File.separator + "wallpaper-changer.exe "
        + path + " 0");
    }
    catch (IOException e) {
      Log.error("Error executing wallpaper changer command: ", e);
    }
  }

}
