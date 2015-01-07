package mgh14.search.live.model.web.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Class for downloading an image via a network
 */
@Component
public class ImageUtils {

  public static final String BASE_SAVE_DIRECTORY = "C:\\Users\\mgh14\\Pictures\\";

  private final Logger Log = LoggerFactory.getLogger(this.getClass());
  private ConcurrentHashMap<String, String> downloadedResources =
    new ConcurrentHashMap<String, String>();

  public String saveImage(String searchStringFolder, String absoluteCurrentFilename) {
    final String filename = absoluteCurrentFilename.substring(
      absoluteCurrentFilename.lastIndexOf("\\"));

    try {
      FileUtils.copyFile(new File(absoluteCurrentFilename),
        new File(BASE_SAVE_DIRECTORY + searchStringFolder + filename));
    }
    catch (IOException e) {
      Log.error("IOException copying file: {}", absoluteCurrentFilename, e);
      return null;
    }

    return absoluteCurrentFilename;
  }

  public boolean canOpenImage(String absoluteFilepath) {
    BufferedImage image;
    try {
      image = ImageIO.read(new File(absoluteFilepath));
    }
    catch (Exception e) {
      return false;
    }

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

  public String copyFileToBitmap(String path) throws IOException {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("Can't convert image with null " +
        "or empty file path");
    }

    // Parse filename and change to .bmp filename
    final Path filepath = Paths.get(path);
    final String fullFilename = filepath.getFileName().toString();
    final String nameOfFile = fullFilename.substring(0,
      fullFilename.lastIndexOf(".")) + ".bmp";
    final String newFullFilename = path.replace(fullFilename, "\\bmp\\" + nameOfFile);

    //Write the image to the destination as a BMP
    ImageIO.write(ImageIO.read(new File(path)), "bmp",
      new File(newFullFilename));

    return newFullFilename;
  }

  private void downloadImageToFile(String imageUrl, String ROOT_DIR,
      String destinationFile) throws IOException {

    final String absoluteFilename = getAbsolutePath(ROOT_DIR, destinationFile);

    final URL website = new URL(imageUrl);
    final ReadableByteChannel rbc = Channels.newChannel(website.openStream());
    final FileOutputStream fileOutputStream = new FileOutputStream(absoluteFilename);
    fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
  }

  private String getAbsolutePath(String ROOT_DIR, String destination) {
    String absoluteFilename = destination;
    if (!absoluteFilename.contains(ROOT_DIR)) {
      absoluteFilename = ROOT_DIR + destination;
    }
    return absoluteFilename;
  }

}