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
 * Class for parsing HTML documents retrieved from
 * the web with Jsoup.
 */
@Component
public class ResourceHtmlDocumentParser {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());

  public List<URI> getResourceUrisFromRetrievedResultsDoc(URI searchUri,
      int numResultsToGet) {

    final List<URI> pageResources = new LinkedList<URI>();
    final Document doc = getSearchDocument(searchUri);
    if (doc != null) {
      final Elements resourcesDetails = doc.select("a[m]");
      for (Element link : resourcesDetails) {
        if (pageResources.size() >= numResultsToGet) {
          Log.info("Reached result limit of {}. Not adding more resources",
            numResultsToGet);
          break;
        }

        pageResources.add(parseResourceFromLink(link.attr("abs:m")));
      }
    }

    return pageResources;
  }

  private Document getSearchDocument(URI searchUri) {
    Document doc;
    try {
      doc = Jsoup.connect(searchUri.toString()).followRedirects(true)
        .userAgent("").get();
    }
    catch (IOException e) {
      Log.error("IOException (is the network connected?): ", e);
      doc = null;
    }

    return doc;
  }

  private URI parseResourceFromLink(String resourceAttr) {
    final String resourceAttributeName = "imgurl:";
    final int imgUrlStart = resourceAttr.indexOf(resourceAttributeName) +
      resourceAttributeName.length();
    String url = resourceAttr.substring(imgUrlStart,
      resourceAttr.indexOf(",", imgUrlStart)).replace("\"", "");
    return URI.create(url);
  }
}
