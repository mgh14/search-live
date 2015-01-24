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
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;

import mgh14.search.live.model.ParamNames;
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

  private static final String BITMAP_EXTENSION = "bmp";

  @Autowired
  private Preferences preferences;
  @Autowired
  private mgh14.search.live.model.web.util.FileUtils fileUtils;

  private ConcurrentHashMap<String, String> downloadedResources =
    new ConcurrentHashMap<String, String>();

  public String saveImage(String searchStringFolder,
      String absoluteCurrentFilename) {

    if (absoluteCurrentFilename == null ||
      absoluteCurrentFilename.isEmpty()) {
      return null;
    }

    final String filename = fileUtils.
      getResourceFilenameFromPath(absoluteCurrentFilename);

    try {
      FileUtils.copyFile(new File(absoluteCurrentFilename),
        new File(preferences.get(ParamNames.RESOURCE_SAVE_DIR, "") +
          searchStringFolder + filename));
    }
    catch (IOException e) {
      Log.error("IOException copying file: {}", absoluteCurrentFilename,
        e);
      return null;
    }

    Log.debug("Image saved: [{}]", preferences.get(
      ParamNames.RESOURCE_SAVE_DIR, "") + searchStringFolder
      + filename);
    return absoluteCurrentFilename;
  }

  public boolean isBitmap(String absoluteFilepathToImage) {
    final String extension = fileUtils.getFileExtension(absoluteFilepathToImage);

    return !extension.isEmpty() &&
      extension.toLowerCase().equals(BITMAP_EXTENSION);
  }

  public String convertImageToBitmap(String absoluteFilepathToImage) {
    if (!canOpenImage(absoluteFilepathToImage)) {
      return null;
    }
    if (isBitmap(absoluteFilepathToImage)) {
      return absoluteFilepathToImage;
    }

    //Create file for the source
    final File inputImage = new File(absoluteFilepathToImage);

    //Read the file to a BufferedImage
    BufferedImage image = null;
    try {
      image = ImageIO.read(inputImage);
    }
    catch (IOException e) {
      Log.error("Error reading in {} file:", absoluteFilepathToImage, e);
    }

    //Create a file for the output
    final File output = new File(absoluteFilepathToImage
      .substring(0, absoluteFilepathToImage.lastIndexOf("."))
      + "." + BITMAP_EXTENSION);

    //Write the image to the destination as a BMP
    try {
      ImageIO.write(image, BITMAP_EXTENSION, output);
    }
    catch (IOException e) {
      Log.error("Error writing bitmap to file: ", e);
    }

    return output.getAbsolutePath();
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

  public String downloadImage(String resourceUrl, String rootDir,
    String localFilename) throws IOException {

    if (downloadedResources.keySet().contains(resourceUrl)) {
      Log.info("Media URL [{}] already exists" +
        " on disk. Skipping download...", resourceUrl);
      return downloadedResources.get(resourceUrl);
    }

    Log.info("Downloading URL [{}]...", resourceUrl);
    try {
      downloadImageToFile(resourceUrl, rootDir, localFilename);
    } catch (IOException e) {
      final String exceptionMessage = "IOException: Couldn\'t " +
        "download image: [" + resourceUrl + "]";
      Log.error(exceptionMessage);
      throw new IOException(exceptionMessage);
    }

    downloadedResources.put(resourceUrl, localFilename);
    return localFilename;
  }

  private void downloadImageToFile(String imageUrl, String rootDir,
      String destinationFile) throws IOException {

    final String absoluteFilename = getAbsolutePath(rootDir, destinationFile);

    final URL website = new URL(imageUrl);
    final InputStream webStream = website.openStream();
    final ReadableByteChannel rbc = Channels.newChannel(webStream);
    final FileOutputStream fileOutputStream = new FileOutputStream(
      absoluteFilename);

    long amountTransferred = fileOutputStream.getChannel()
      .transferFrom(rbc, 0, Long.MAX_VALUE);
    if (amountTransferred < 1) {
      Log.error("Amount transferred for [{}] is {}", absoluteFilename,
        amountTransferred);
    }
    else {
      Log.debug("Finished downloading image to file: [{}] to [{}]",
        imageUrl, absoluteFilename);
    }

    webStream.close();
    rbc.close();
    fileOutputStream.close();
  }

  private String getAbsolutePath(String rootDir, String destination) {
    String absoluteFilename = destination;
    if (!absoluteFilename.contains(rootDir)) {
      absoluteFilename = rootDir + destination;
    }
    return absoluteFilename;
  }

}
