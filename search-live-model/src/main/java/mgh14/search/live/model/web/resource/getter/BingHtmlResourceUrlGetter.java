package mgh14.search.live.model.web.resource.getter;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import mgh14.search.live.model.web.util.ResourceHtmlDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  private ResourceHtmlDocumentParser docParser;

  private List<URI> allResourceUris;
  private String resourceType;
  private String searchString;
  private AtomicInteger numResultsToGet;
  private AtomicInteger pageToGet;

  public BingHtmlResourceUrlGetter() {
    allResourceUris = new LinkedList<URI>();
    setResourceType(null);
    setSearchString(null);

    numResultsToGet = new AtomicInteger();
    setNumResultsToGet(0);

    pageToGet = new AtomicInteger();
    setPageToGet(FIRST_PAGE_TO_GET);
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }

  public void setNumResultsToGet(int numResultsToGet) {
    this.numResultsToGet.set(numResultsToGet);
  }

  @Override
  public int getNumPagesRetrieved() {
    return pageToGet.get() - 1;
  }

  private void setPageToGet(int pageToGet) {
    if (pageToGet < 1) {
      throw new IllegalArgumentException("Can't get a page less than one.");
    }
    this.pageToGet.set(pageToGet);
  }

  @Override
  public List<String> getResources() {
    // fetch the resource URI's
    final List<URI> pageResources = docParser.getResourceUrisFromRetrievedResultsDoc(
      URI.create(HOST + resourceType + SEARCH_PATH +
        searchString.replaceAll(" ", "+")), numResultsToGet.get());
    allResourceUris.addAll(pageResources);
    Log.info("Retrieved {} URI's from document with search string \"{}\"",
      pageResources.size(), searchString);

    // prepare search url for next page of results
    if (pageResources.size() > 0) {
      prepareSearchStringForPagination();
    }

    // convert URI's to strings
    final List<String> pageResourceStrs = new LinkedList<String>();
    for (URI resource : pageResources) {
      pageResourceStrs.add(resource.toString());
    }
    return pageResourceStrs;
  }

  private void prepareSearchStringForPagination() {
    if (pageToGet.get() != FIRST_PAGE_TO_GET) {
      String newSearchString =  searchString.substring(0,
        searchString.lastIndexOf(PAGE_PARAM));
      newSearchString += PAGE_PARAM + (pageToGet.get() + 1);
      searchString = newSearchString;
    }
    else {
      // add 'page <x>' to query string for further pagination
      searchString += PAGE_PARAM + (FIRST_PAGE_TO_GET + 1);
    }
    pageToGet.incrementAndGet();  // result not needed

    Log.debug("Next (paginated) search string assigned: [{}]", searchString);
  }

}
