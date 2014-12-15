package mgh14.search.live.web;

import java.net.URI;
import java.util.List;

/**
 * Interface for retrieving a list of URI resources
 */
public interface ResourceUrlGetter {

  List<URI> getResources(String searchString, int pageToGet);
}
