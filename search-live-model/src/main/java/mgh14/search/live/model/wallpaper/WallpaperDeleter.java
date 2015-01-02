package mgh14.search.live.model.wallpaper;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Deletes expired wallpaper images from the temp folder that
 * have been downloaded for past wallpaper cycles.
 */
@Component
public class WallpaperDeleter {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  public static final File resourceFolder =
    new File("C:\\Users\\mgh14\\Pictures\\screen-temp\\");

  public void deleteFile(Path filepath) {
    if(filepath == null) {
      return;
    }

    Log.info("Deleting file: [{}]...", filepath);

    try {
      Files.delete(filepath);
      Log.debug("File deletion finished for [{}].", filepath);
    } catch (NoSuchFileException x) {
      Log.info("No such file or directory: [{}]", filepath);
    } catch (DirectoryNotEmptyException x) {
      Log.info("Directory not empty: [{}]", filepath);
    } catch (IOException x) {
      x.printStackTrace();
    }

  }

  public void deleteExpiredFiles(final File folder) {
    final long currentTime = System.currentTimeMillis();
    Log.info("Deleting expired pictures for timestamp {}...", currentTime);

    if(folder != null && folder.listFiles() != null) {
      long expiryPeriod = 24 * 60 * 60 * 1000;   // 1 day (in milliseconds)

      for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
          deleteExpiredFiles(fileEntry);
          continue;
        }

        final String filename = fileEntry.getName();
        final String timestampStr = filename.substring(filename.lastIndexOf("-") + 1,
          filename.lastIndexOf("."));
        final long timestamp = Long.parseLong(timestampStr);
        if ((currentTime - timestamp) >= expiryPeriod) {
          deleteExpiredFile(fileEntry.toPath());
        }
      }
    }

    Log.info("Finished deleting expired pictures for timestamp {}", currentTime);
  }

  public void deleteAllResources() {
    Log.info("Deleting all resources...");
    if(resourceFolder.listFiles() != null) {
      for (final File fileEntry : resourceFolder.listFiles()) {
        fileEntry.delete();
      }
    }
  }

  private void deleteExpiredFile(Path filepath) {
    deleteFile(filepath);
  }

}
