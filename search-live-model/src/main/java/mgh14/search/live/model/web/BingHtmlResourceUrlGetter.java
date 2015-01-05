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
  private static final String PAGE_PARAM = " page ";
  private static final int FIRST_PAGE_TO_GET = 1;

  private List<URI> allResourceUris;
  private String resourceType;
  private String searchString;
  private int numResultsToGet;
  private int pageToGet;

  public BingHtmlResourceUrlGetter() {
    allResourceUris = new LinkedList<URI>();
    setResourceType(null);
    setSearchString(null);
    setNumResultsToGet(0);
    setPageToGet(FIRST_PAGE_TO_GET);
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
  public int getNumPagesRetrieved() {
    return pageToGet - 1;
  }

  private void setPageToGet(int pageToGet) {
    if (pageToGet < 1) {
      throw new IllegalArgumentException("Can't get a page less than one.");
    }
    this.pageToGet = pageToGet;
  }

  @Override
  public List<URI> getResources() {
    // fetch the resource URI's
    final List<URI> pageResources = new LinkedList<URI>();
    final Document doc = getSearchDocument(searchString);
    if (doc != null) {
      final Elements resourcesDetails = doc.select("a[m]");
      for (Element link : resourcesDetails) {
        if (pageResources.size() >= numResultsToGet) {
          Log.info("Reached result limit of {}. Not adding more resources",
            numResultsToGet);
          break;
        }

        pageResources.add(parseResource(link.attr("abs:m")));
      }
    }
    allResourceUris.addAll(pageResources);
    Log.info("Retrieved {} URI's from document with search string \"{}\"",
      pageResources.size(), searchString);

    // prepare search url for next page of results
    if (pageResources.size() > 0) {
      prepareSearchStringForPagination();
    }

    return pageResources;
  }

  private void prepareSearchStringForPagination() {
    if (pageToGet != FIRST_PAGE_TO_GET) {
      String newSearchString =  searchString.substring(0,
        searchString.lastIndexOf(PAGE_PARAM));
      newSearchString += PAGE_PARAM + (pageToGet + 1);
      searchString = newSearchString;
    }
    else {
      // add 'page <x>' to query string for further pagination
      searchString += PAGE_PARAM + (FIRST_PAGE_TO_GET + 1);
    }
    pageToGet++;

    Log.debug("Next (paginated) search string assigned: [{}]", searchString);
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
