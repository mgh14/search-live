package mgh14.search.live.web;

import java.net.URI;
import java.util.List;

/**
 * Interface for retrieving a list of URI resources
 */
public interface ResourceGetter {

  List<URI> getResources(String searchString, int pageToGet);
}
