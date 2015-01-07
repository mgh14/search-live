package mgh14.search.live.model.wallpaper;

import java.io.File;

import mgh14.search.live.model.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Deletes expired wallpaper images from the temp folder that
 * have been downloaded for past wallpaper cycles.
 */
@Component
public class ExpiredResourcesDeleter {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private FileUtils fileUtils;

  public void deleteExpiredFiles(final File folder) {
    final long currentTime = System.currentTimeMillis();
    Log.info("Deleting expired pictures for timestamp {}...", currentTime);

    if(folder != null && folder.listFiles() != null) {
      long expiryPeriod = 5 * 24 * 60 * 60 * 1000;   // 5 days (in milliseconds)

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
          fileUtils.deleteFile(fileEntry.toPath());
        }
      }
    }

    Log.info("Finished deleting expired pictures for timestamp {}", currentTime);
  }

}
