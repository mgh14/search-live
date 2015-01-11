package mgh14.search.live.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for file operations
 */
@Component
public class FileUtils {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  public static final String FILE_SEPARATOR =
    (String) System.getProperties().get("file.separator");

  private String resourceFolder;

  public FileUtils() {
    resourceFolder = null;
  }

  @PostConstruct
  public void constructTemporaryResourceFolder() {
    resourceFolder = constructFilepathWithSeparator(
      "C:", "Users", "mgh14", "Pictures", "screen-temp");
  }

  public String getResourceFolder() {
    return resourceFolder;
  }

  public String constructFilepathWithSeparator(String... dirs) {
    String filepath = "";
    for (String dir : dirs) {
      filepath += dir + FILE_SEPARATOR;
    }
    return filepath;
  }

  public void deleteFile(Path filepath) {
    if(filepath == null) {
      return;
    }

    Log.info("Deleting file: [{}]...", filepath);

    try {
      Files.delete(filepath);
    } catch (NoSuchFileException e) {
      Log.error("No such file or directory: [{}]", filepath);
    } catch (DirectoryNotEmptyException e) {
        deleteDirectory(filepath);
    } catch (IOException x) {
      Log.error("IOException deleting file: ", x);
    }

    Log.debug("File deletion finished for [{}].", filepath);
  }

  public void deleteDirectory(Path directory) {
    try {
      org.apache.commons.io.FileUtils.deleteDirectory(directory.toFile());
    }
    catch (IOException e) {
      Log.error("Error deleting directory: ", e);
    }
  }

  public void deleteAllFiles(File resourceFolder) {
    Log.info("Deleting all resources...");

    final File[] folderFiles = resourceFolder.listFiles();
    if(folderFiles != null) {
      for (final File fileEntry : folderFiles) {
        deleteFile(fileEntry.toPath());
      }
    }
  }

}
