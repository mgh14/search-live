package mgh14.search.live.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Deletes expired wallpaper images from the temp folder that
 * have been downloaded for past wallpaper cycles.
 */
public class WallpaperDeleter {

  public void deleteExpiredFiles(final File folder) {
    final long currentTime = System.currentTimeMillis();
    System.out.println("Deleting expired pictures for timestamp " + currentTime + "...");

    if(folder != null && folder.listFiles() != null) {
      long expiryPeriod = 24 * 60 * 60 * 1000;   // 1 day (in milliseconds)

      for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
          deleteExpiredFiles(fileEntry);
          continue;
        }

        final String filename = fileEntry.getName();
        final String timestampStr = fileEntry.getName()
          .substring(filename.lastIndexOf("-") + 1, filename.lastIndexOf("."));
        final long timestamp = Long.parseLong(timestampStr);
        if ((currentTime - timestamp) >= expiryPeriod) {
          deleteExpiredFile(fileEntry.toPath());
        }
      }
    }

    System.out.println("Finished deleting expired pictures for timestamp " + currentTime + "...");
  }

  private void deleteExpiredFile(Path filepath) {
    if(filepath == null) {
      return;
    }

    System.out.println("Deleting file: " + filepath + "...");

    try {
      Files.delete(filepath);
    } catch (NoSuchFileException x) {
      System.out.format("%s: no such file or directory%n", filepath);
    } catch (DirectoryNotEmptyException x) {
      System.out.format("%s not empty%n", filepath);
    } catch (IOException x) {
      x.printStackTrace();
    }
  }

}
