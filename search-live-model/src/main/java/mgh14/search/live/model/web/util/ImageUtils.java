package mgh14.search.live.model.web.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for downloading an image via a network
 */
@Component
public class ImageUtils {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  @Autowired
  private mgh14.search.live.model.web.util.FileUtils fileUtils;

  private String savedPicsDir = null;
  private ConcurrentHashMap<String, String> downloadedResources =
    new ConcurrentHashMap<String, String>();

  @PostConstruct
  public void setBaseSaveDirectory() {
    savedPicsDir = fileUtils.constructFilepathWithSeparator(
      "C:", "Users", "mgh14", "Pictures", "Search-live-saves");
  }

  public String saveImage(String searchStringFolder, String absoluteCurrentFilename) {
    if (absoluteCurrentFilename == null || absoluteCurrentFilename.isEmpty()) {
      return null;
    }

    final String filename = absoluteCurrentFilename.substring(
      absoluteCurrentFilename.lastIndexOf("\\"));

    try {
      FileUtils.copyFile(new File(absoluteCurrentFilename),
        new File(savedPicsDir + searchStringFolder + filename));
    }
    catch (IOException e) {
      Log.error("IOException copying file: {}", absoluteCurrentFilename, e);
      return null;
    }

    Log.debug("Image saved: [{}]", savedPicsDir + searchStringFolder + filename);
    return absoluteCurrentFilename;
  }

  public boolean canOpenImage(String absoluteFilepath) {
    // create file input stream (so we can close it later)
    FileInputStream inputStream;
    try {
      inputStream = new FileInputStream(absoluteFilepath);
    }
    catch (FileNotFoundException e) {
      Log.error("File not found: ", e);
      return false;
    }

    // read in the file to a buffered image
    BufferedImage image;
    try {
      image = ImageIO.read(inputStream);
    }
    catch (Exception e) {
      Log.error("Image read error: ", e);
      return false;
    }

    // close the image stream
    try {
        inputStream.close();
    }
    catch (IOException e) {
      Log.error("IOException: ", e);
      return false;
    }

    // make sure the image is actually an image (instead
    // of the weird 2x1 image files that are sometimes
    // "downloaded")
    final int pixelTolerance = 5;
    return (image != null && image.getWidth() > pixelTolerance
      && image.getHeight() > pixelTolerance);
  }

  public String downloadImage(String resourceUrl, String ROOT_DIR,
    String localFilename) throws IOException {

    if (downloadedResources.keySet().contains(resourceUrl)) {
      Log.info("Media URL [{}] already exists" +
        " on disk. Skipping download...", resourceUrl);
      return downloadedResources.get(resourceUrl);
    }

    Log.info("Downloading URL [{}]...", resourceUrl);
    try {
      downloadImageToFile(resourceUrl, ROOT_DIR, localFilename);
    } catch (IOException e) {
      final String exceptionMessage = "IOException: Couldn\'t " +
        "download image: [" + resourceUrl + "]";
      Log.error(exceptionMessage);
      throw new IOException(exceptionMessage);
    }

    downloadedResources.put(resourceUrl, localFilename);
    return localFilename;
  }

  private void downloadImageToFile(String imageUrl, String ROOT_DIR,
      String destinationFile) throws IOException {

    final String absoluteFilename = getAbsolutePath(ROOT_DIR, destinationFile);

    final URL website = new URL(imageUrl);
    final InputStream webStream = website.openStream();
    final ReadableByteChannel rbc = Channels.newChannel(webStream);
    final FileOutputStream fileOutputStream = new FileOutputStream(absoluteFilename);

    long amountTransferred = fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    if (amountTransferred < 1) {
      Log.error("Amount transferred for [{}] is {}", absoluteFilename, amountTransferred);
    }
    else {
      Log.debug("Finished downloading image to file: [{}] to [{}]",
        imageUrl, absoluteFilename);
    }

    webStream.close();
    rbc.close();
    fileOutputStream.close();
  }

  private String getAbsolutePath(String ROOT_DIR, String destination) {
    String absoluteFilename = destination;
    if (!absoluteFilename.contains(ROOT_DIR)) {
      absoluteFilename = ROOT_DIR + destination;
    }
    return absoluteFilename;
  }

}
