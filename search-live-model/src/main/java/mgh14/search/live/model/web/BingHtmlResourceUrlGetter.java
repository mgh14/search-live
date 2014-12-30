package mgh14.search.live.model.web;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Uses Bing's search page to fetch resource URL's
 * (i.e. uses the HTML of the search page from a
 * regular GET request--no authorization is needed)
 */
@Component
public class BingHtmlResourceUrlGetter implements ResourceUrlGetter {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  private static final String HOST = "https://www.bing.com/";
  private static final String SEARCH_PATH = "/search?q=";

  private List<URI> allResourceUris;
  private String resourceType;
  private String searchString;
  private int numResultsToGet;

  public BingHtmlResourceUrlGetter() {
    allResourceUris = new LinkedList<URI>();
    setResourceType(null);
    setSearchString(null);
    setNumResultsToGet(0);
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }

  public void setNumResultsToGet(int numResultsToGet) {
    this.numResultsToGet = numResultsToGet;
  }

  @Override
  public List<URI> getResources() {
    // Recycle wallpaper if it has already been retrieved
    if (!allResourceUris.isEmpty()) {
      return allResourceUris;
    }

    // Otherwise, fetch the wallpaper URL's
    final Document doc = getSearchDocument(searchString);
    if (doc != null) {
      final Elements resourcesDetails = doc.select("a[m]");
      for (Element link : resourcesDetails) {
        if (allResourceUris.size() >= numResultsToGet) {
          Log.info("Reached result limit of {}. Not adding more resources",
            numResultsToGet);
          break;
        }

        allResourceUris.add(parseResource(link.attr("abs:m")));
      }
    }

    return allResourceUris;
  }

  private Document getSearchDocument(String searchString) {
    if(searchString == null || searchString.isEmpty()) {
        return null;
      }

    final URI searchUri = URI.create(HOST + resourceType + SEARCH_PATH +
      searchString.replaceAll(" ", "+"));
    Document doc;
    try {
      doc = Jsoup.connect(searchUri.toString()).followRedirects(true).get();
    }
    catch (IOException e) {
      Log.error("IOException (is the network connected?): ", e);
      doc = null;
    }

    return doc;
  }

  private URI parseResource(String resourceAttr) {
    final String resourceAttributeName = "imgurl:";
    final int imgUrlStart = resourceAttr.indexOf(resourceAttributeName) +
      resourceAttributeName.length();
    String url = resourceAttr.substring(imgUrlStart,
      resourceAttr.indexOf(",", imgUrlStart)).replace("\"", "");
    return URI.create(url);
  }
}
