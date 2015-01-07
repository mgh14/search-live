package mgh14.search.live.model.wallpaper;

import java.io.IOException;

import mgh14.search.live.model.web.util.ImageUtils;
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

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private ImageUtils imageUtils;

  public void setDesktopWallpaper(String path) {
    if (path == null || path.isEmpty()) {
      Log.error("Invalid null/empty file name");
      return;
    }

    Log.info("Setting image: [{}]...", path);

    copyAndSetWallpaper(path);
  }

  private void copyAndSetWallpaper(String path) {
    Log.debug("Copying image to bitmap: [{}]", path);
    // copy image to bitmap type (.bmp)
    String newPath = null;
    try {
      newPath = imageUtils.copyFileToBitmap(path);
    }
    catch (IOException e) {
      Log.error("IOError converting image to bitmap: \n", e);
      return;
    }

    Log.debug("Setting bitmap image as background: [{}]", newPath);
    // If image is copied successfully, set it as the desktop wallpaper
    if (newPath != null) {
      final Runtime runtime = Runtime.getRuntime();
      try {
        runtime.exec("reg add \"HKCU\\Control Panel\\Desktop\" /v Wallpaper " +
          "/t REG_SZ /d \"" + newPath +"\" /f");
        runtime.exec("reg add \"HKCU\\Control Panel\\Desktop\" /v WallpaperStyle " +
          "/t REG_SZ /d 2 /f");
        runtime.exec("RUNDLL32.EXE user32.dll,UpdatePerUserSystemParameters");
      }
      catch (IOException e) {
        Log.error("IOError setting file [{}] to desktop: ", path, e);
      }
    }
  }

}
