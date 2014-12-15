package mgh14.search.live.web;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Uses Bing to fetch resource URL's
 */
public class BingApiResourceGetter {

  private static final String HOST_PATH = "https://api.datamarket.azure.com/Bing/Search/v1/";
  private static final String QUERY_PARAMS = "?$format=json&Query=%27{}%27";
  private static final String TOP_PARAM = "$top=";
  private static final String AUTH_HEADER_VALUE = "Basic {}";

  private List<URI> allResourceUris;
  private String searchUrl;
  private String nextSearchUrl;

  public BingApiResourceGetter(String resourceType, int numResults) {
    allResourceUris = new LinkedList<URI>();
    searchUrl = HOST_PATH + resourceType + QUERY_PARAMS + "&"
      + TOP_PARAM + numResults;
    nextSearchUrl = null;
  }

  public List<URI> getResources(String authToken, String searchString, int pageToGet) {
    final List<URI> pagedUris = new LinkedList<URI>();
    final JSONArray array = getMediaUrlArray(authToken, searchString, pageToGet);
    if (array != null) {
      for (int i = 0; i < array.length(); i++) {
        final JSONObject entry = (JSONObject) array.get(i);
        final URI mediaUrl = URI.create(entry.get("MediaUrl").toString());
        System.out.println("Procured MediaUrl: [" + mediaUrl + "]");

        pagedUris.add(mediaUrl);
        allResourceUris.add(mediaUrl);
      }
    }

    return pagedUris;
  }

  public List<URI> getAllResourceUris() {
    return allResourceUris;
  }

  private JSONArray getMediaUrlArray(String authToken, String searchString, int pageToGet) {
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
        .header("Authorization", AUTH_HEADER_VALUE.replace("{}", authToken)).asJson();
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
