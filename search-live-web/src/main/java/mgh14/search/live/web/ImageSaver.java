package mgh14.search.live.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for downloading an image from the internet
 */
public class ImageSaver {

  private Map<String, String> downloadedResources = new HashMap<String, String>();

  public String saveImage(String resourceUrl, String ROOT_DIR, String localFilename) throws IOException {
    if (downloadedResources.keySet().contains(resourceUrl)) {
      System.out.println("Media URL [" + resourceUrl + "] already exists on disk. Skipping download...");
      return downloadedResources.get(resourceUrl);
    }

    System.out.println("Downloading URL [" + resourceUrl + "]...");
    try {
      downloadImageToFile(resourceUrl, ROOT_DIR, localFilename);
    } catch (IOException e) {
      final String exceptionMessage = "Couldn\'t download image: [" + resourceUrl + "]";
      System.out.println(exceptionMessage);
      throw new IOException(exceptionMessage);
    }

    downloadedResources.put(resourceUrl, localFilename);
    return localFilename;
  }

  private void downloadImageToFile(String imageUrl, String ROOT_DIR, String destinationFile) throws IOException {
    URL url = new URL(imageUrl);
    InputStream is = url.openStream();

    String absoluteFilename = getAbsolutePath(ROOT_DIR, destinationFile);
    OutputStream os = new FileOutputStream(absoluteFilename);

    byte[] b = new byte[2048];
    int length;
    while ((length = is.read(b)) != -1) {
      os.write(b, 0, length);
    }

    is.close();
    os.close();
  }

  private String getAbsolutePath(String ROOT_DIR, String destination) {
    String absoluteFilename = destination;
    if (!absoluteFilename.contains(ROOT_DIR)) {
      absoluteFilename = ROOT_DIR + destination;
    }
    return absoluteFilename;
  }

}
