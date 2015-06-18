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
@Component
public class WindowsWallpaperSetter {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  @Autowired
  private ImageUtils imageUtils;

  public void setDesktopWallpaper(String path) {
    if (path == null || path.isEmpty()) {
      Log.error("Invalid null/empty file name");
      return;
    }
    if (!imageUtils.isBitmap(path)) {
      path = imageUtils.convertImageToBitmap(path);
    }

    Log.info("Setting resource image to background: [{}]...", path);
    /*String as[] = {
            "osascript",
            "-e", "tell application \"Finder\"",
            "-e", "set desktop picture to POSIX file \"" + path + "\"",
            "-e", "end tell"
    };*/
    String as[] = {
            "osascript",
            "-e", "tell application \"System Events\"",
              "-e", "set desktopCount to count of desktops",
              "-e", "repeat with desktopNumber from 1 to desktopCount",
                "-e", "tell desktop desktopNumber",
                  "-e", "set picture to POSIX file \"" + path + "\"",
                "-e", "end tell",
              "-e", "end repeat",
            "-e", "end tell"
    };
    Runtime runtime = Runtime.getRuntime();
    try {
      runtime.exec(as);
      runtime.exec("killall Dock");
    } catch (IOException e) {
      e.printStackTrace();
    }
    /*SPI.INSTANCE.SystemParametersInfo(
      new WinDef.UINT_PTR(SPI.SPI_SETDESKWALLPAPER),
      new WinDef.UINT_PTR(0),
      path,
      new WinDef.UINT_PTR(SPI.SPIF_UPDATEINIFILE | SPI.SPIF_SENDWININICHANGE));*/
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
