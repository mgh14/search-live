package mgh14.search.live.model;

import java.util.HashMap;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.UINT_PTR;
import com.sun.jna.win32.*;

public class WindowsWallpaperSetter {

  public void setDesktopWallpaper(String path) {
    if (path == null || path.isEmpty()) {
      System.out.println("Invalid file name");
      return;
    }

    System.out.println("Setting new image: " + path + ";");

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
