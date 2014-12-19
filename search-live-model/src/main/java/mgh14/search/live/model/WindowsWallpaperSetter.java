package mgh14.search.live.model;

import java.util.HashMap;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.UINT_PTR;
import com.sun.jna.win32.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for setting desktop wallpaper given the path to an image
 * for the Windows operating system.
 */
// TODO: Test in Windows environments other than Windows 7 64-bit
public class WindowsWallpaperSetter {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  public void setDesktopWallpaper(String path) {
    if (path == null || path.isEmpty()) {
      Log.error("Invalid null/empty file name");
      return;
    }

    Log.info("Setting image: [{}]...", path);

    SPI.INSTANCE.SystemParametersInfo(
      new UINT_PTR(SPI.SPI_SETDESKWALLPAPER),
      new UINT_PTR(0),
      path,
      new UINT_PTR(SPI.SPIF_UPDATEINIFILE | SPI.SPIF_SENDWININICHANGE));
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
      UINT_PTR uiAction,
      UINT_PTR uiParam,
      String pvParam,
      UINT_PTR fWinIni
    );
  }
}
