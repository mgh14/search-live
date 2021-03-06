package mgh14.search.live.model.web.util;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Class for parsing HTML documents retrieved from
 * the web.
 */
@Component
public class ResourceHtmlDocumentParser {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  private static final String IMG_RESOURCE_ATTRIBUTE_NAME = "imgurl:";
  private static final int RANDOM_ANCHOR_INDEX_LIMIT = 5;

  private Document currentDoc = null;
  private String nextSearchQuery = null;

  public String getNextSearchQuery() {
    return nextSearchQuery;
  }

  public List<URI> getResourceUrisFromSource(URI searchUri,
      int numResultsToGet) {

    final List<URI> pageResources = new LinkedList<URI>();
    retrieveSearchDocument(searchUri);
    if (currentDoc != null) {
      Log.debug("Search document retrieved.");
      final Elements resourcesDetails = currentDoc.select("a[m]");
      for (Element link : resourcesDetails) {
        if (pageResources.size() >= numResultsToGet) {
          Log.info("Reached result limit of {}. Not adding more resources",
            numResultsToGet);
          break;
        }

        final URI resourceUri = parseResourceFromLink(link.attr("abs:m"));
        if (resourceUri != null) {
          pageResources.add(resourceUri);
        }
      }

      nextSearchQuery = parseNextSearchQuery();
    }
    else {
      Log.debug("No document retrieved for [{}]", searchUri);
    }

    return pageResources;
  }

  private String parseNextSearchQuery() {
    if (currentDoc == null) {
      Log.error("Current document has not been retrieved! " +
        "Cannot retrieve URI of the next search.");
      return null;
    }

    final Elements newSearchQueries = currentDoc.select("a[title*=Search For");

    if (newSearchQueries.isEmpty()) {
      return "";
    }
    final int randomAnchorIndex = (newSearchQueries.size() >= RANDOM_ANCHOR_INDEX_LIMIT) ?
      new Random().nextInt(RANDOM_ANCHOR_INDEX_LIMIT) :
      new Random().nextInt(newSearchQueries.size());

    final String href = newSearchQueries.get(randomAnchorIndex).attr("abs:href");
    final int locOfQueryParam = href.lastIndexOf("q=") + 2;
    return href.substring(locOfQueryParam, href.indexOf("&", locOfQueryParam));
  }

  private void retrieveSearchDocument(URI searchUri) {
    try {
      currentDoc = Jsoup.connect(searchUri.toString()).followRedirects(true)
        .userAgent("").timeout(2000).get();
    }
    catch (IOException e) {
      Log.error("IOException (is the network connected?): ", e);
    }
  }

  private URI parseResourceFromLink(String resourceAttr) {
    final int imgUrlStart = resourceAttr.indexOf(IMG_RESOURCE_ATTRIBUTE_NAME) +
      IMG_RESOURCE_ATTRIBUTE_NAME.length();
    String url = resourceAttr.substring(imgUrlStart,
      resourceAttr.indexOf(",", imgUrlStart)).replace("\"", "");

    URI resourceUri = null;
    try {
      resourceUri = URI.create(url);
    } catch (IllegalArgumentException e) {
      Log.error("Error creating [{}] as URI: ", url, e);
    }

    return resourceUri;
  }
}
