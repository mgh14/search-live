package mgh14.search.live.model.web.resource.getter;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Bing's API to fetch resource URL's
 *
 * Note: this class automatically uses pagination,
 * and this feature can't be turned off right now
 */
public class BingApiResourceUrlGetter implements ResourceUrlGetter {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());

  private static final String HOST_PATH = "https://api.datamarket.azure.com/Bing/Search/v1/";
  private static final String QUERY_PARAMS = "?$format=json&Query=%27{}%27";
  private static final String TOP_PARAM = "$top=";
  private static final String AUTH_HEADER_VALUE = "Basic {}";

  private String authHeader;
  private List<URI> allResourceUris;
  private String searchString;
  private String searchUrl;
  private String nextSearchUrl;
  private int pageToGet = 1;

  public BingApiResourceUrlGetter(String authToken, String resourceType, int numResults) {
    setAuthHeader(authToken);
    allResourceUris = new LinkedList<URI>();
    searchUrl = HOST_PATH + resourceType + QUERY_PARAMS + "&"
      + TOP_PARAM + numResults;
    nextSearchUrl = null;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }

  public void setAuthHeader(String authToken) {
    this.authHeader = AUTH_HEADER_VALUE.replace("{}", authToken);
  }

  public int getNumPagesRetrieved() {
    return pageToGet - 1;
  }

  public List<String> getResources() {
    final List<String> pagedUris = new LinkedList<String>();
    final JSONArray array = getMediaUrlArray(searchString, pageToGet++);
    if (array != null) {
      for (int i = 0; i < array.length(); i++) {
        final JSONObject entry = (JSONObject) array.get(i);
        final URI mediaUrl = URI.create(entry.get("MediaUrl").toString());
        Log.info("Procured MediaUrl: [{}]", mediaUrl);

        pagedUris.add(mediaUrl.toString());
        allResourceUris.add(mediaUrl);
      }
    }

    return pagedUris;
  }

  public List<URI> getAllResourceUris() {
    return allResourceUris;
  }

  private JSONArray getMediaUrlArray(String searchString, int pageToGet) {
    if(searchString == null || searchString.isEmpty()) {
      return null;
    }

    // For pagination
    String destinationUrl = nextSearchUrl;
    if(pageToGet == 1) {
      destinationUrl = searchUrl.replace("{}", searchString.replace(" ", "+"));
    }

    // get the response
    HttpResponse<JsonNode> response;
    try {
      response = Unirest.get(destinationUrl)
        .header("Authorization", authHeader).asJson();
    }
    catch (UnirestException e) {
      e.printStackTrace();
      return null;
    }

    // parse resources
    nextSearchUrl = null;
    try {
      JsonNode body = response.getBody();
      JSONObject bodyObject = body.getObject();
      JSONObject responseObjectByNameD = bodyObject.getJSONObject("d");

      nextSearchUrl = (responseObjectByNameD.get("__next")) + "&$format=json";
      return responseObjectByNameD.getJSONArray("results");
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    return null;
  }

}
