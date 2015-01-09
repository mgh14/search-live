package mgh14.search.live.model.wallpaper;

import java.io.IOException;
import java.util.HashMap;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;
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
    String newPath;
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
      SPI.INSTANCE.SystemParametersInfo(
        new WinDef.UINT_PTR(SPI.SPI_SETDESKWALLPAPER),
        new WinDef.UINT_PTR(0),
        path,
        new WinDef.UINT_PTR(SPI.SPIF_UPDATEINIFILE | SPI.SPIF_SENDWININICHANGE));
    }

  }

  private interface SPI extends StdCallLibrary {

    //from MSDN article
    long SPI_SETDESKWALLPAPER = 20;
    long SPIF_UPDATEINIFILE = 0x01;
    long SPIF_SENDWININICHANGE = 0x02;

    SPI INSTANCE = (SPI) Native.loadLibrary("user32", SPI.class, new HashMap<Object, Object>() {
      {
        put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
        put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
      }
    });

    boolean SystemParametersInfo(
      WinDef.UINT_PTR uiAction,
      WinDef.UINT_PTR uiParam,
      String pvParam,
      WinDef.UINT_PTR fWinIni
    );
  }
}
