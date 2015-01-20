package mgh14.search.live.model.web.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Observable;
import java.util.concurrent.ExecutorService;

import mgh14.search.live.model.ParamNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class for file operations
 */
@Component
public class FileUtils extends Observable {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  public static final String RESOURCE_FILENAME_PREPEND = "rsrc";
  public static final String RESOURCE_FILENAME_TIMESTAMP_SEPARATOR = "-";
  public static final String RESOURCES_SUCCESSFULLY_DELETED_MESSAGE
    = "Resources deleted.";
  public static final String RESOURCES_FAILED_TO_DELETE_MESSAGE =
    "Failed to delete resources!";

  @Autowired
  private ExecutorService executorService;

  private String resourceDir;

  public FileUtils() {
    resourceDir = System.getProperty(ParamNames.USER_HOME) +
      File.separator + constructFilepathWithSeparator("Pictures",
      "screen-temp");
  }

  public String getResourceDir() {
    return resourceDir;
  }

  public String constructFilepathWithSeparator(String... dirs) {
    String filepath = "";
    for (String dir : dirs) {
      filepath += dir + File.separator;
    }
    return filepath;
  }

  public String getRelativeResourceFilename(String resourceStr, int downloadNum) {
    // construct (local) filename
    final String filetype = resourceStr.substring(resourceStr.lastIndexOf("."));
    return getResourceDir() + RESOURCE_FILENAME_PREPEND +
      downloadNum + RESOURCE_FILENAME_TIMESTAMP_SEPARATOR +
      System.currentTimeMillis() + filetype;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void makeFileReadableAndWriteable(String filename) {
    final File file = new File(filename);
    file.setReadable(true);
    file.setWritable(true);
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

  public void deleteAllFiles(final File resourceFolder) {
    Log.info("Starting resource deletion thread (deleting " +
      "all resources)...");

    executorService.execute(new Runnable() {
    @Override
    public void run() {
      final File[] folderFiles = resourceFolder.listFiles();
      if (folderFiles == null) {
        Log.error("Error deleting files in folder [{}]: " +
            "null returned from file discovery",
          resourceFolder);
        notifyObserversWithMessage(RESOURCES_FAILED_TO_DELETE_MESSAGE);
        return;   // terminate thread
      }

      for (final File fileEntry : folderFiles) {
        deleteFile(fileEntry.toPath());
      }

      notifyObserversWithMessage(RESOURCES_SUCCESSFULLY_DELETED_MESSAGE);
    }
    });
  }

  private void notifyObserversWithMessage(String message) {
    setChanged();
    notifyObservers(message);
  }

}
