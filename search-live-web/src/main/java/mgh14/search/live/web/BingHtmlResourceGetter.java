package mgh14.search.live.web;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 */
public class BingHtmlResourceGetter {
  private static final String HOST = "https://www.bing.com/";
  private static final String SEARCH_PATH = "/search?q=";

  private List<URI> resourceUris;
  private String searchUrl;
  
    
  public BingHtmlResourceGetter(String resourceType) {
    searchUrl = HOST + resourceType + SEARCH_PATH;
    resourceUris = new LinkedList<URI>();
  }

  public List<URI> getResources(String searchString) {
    final Document doc = getSearchDocument(searchString);

      final Elements resourcesDetails = doc.select("a[m]");
    for (Element link : resourcesDetails) {
        resourceUris.add(parseResource(link.attr("abs:m")));
      }

      return resourceUris;
  }

  private Document getSearchDocument(String searchString) {
    if(searchString == null || searchString.isEmpty()) {
        return null;
      }

      final URI searchUri = URI.create(searchUrl + searchString.replaceAll(" ", ""));
    Document doc;
    try {
        doc = Jsoup.connect(searchUri.toString()).followRedirects(true).get();
      }
    catch (IOException e) {
        e.printStackTrace();
        doc = null;
      }

      return doc;
  }

  private URI parseResource(String resourceAttr) {
    final String resourceAttributeName = "imgurl:";
    final int imgUrlStart = resourceAttr.indexOf(resourceAttributeName) + resourceAttributeName.length();
    String url = resourceAttr.substring(imgUrlStart, resourceAttr.indexOf(",", imgUrlStart)).replace("\"", "");
    return URI.create(url);
  }
}
