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
@Component
public class MacWallpaperSetter {

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
        final String commandArgs[] = {
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
        final Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(commandArgs);
        } catch (IOException e) {
            Log.error("Error executing desktop wallpaper command: ", e);
        }

    }

}
