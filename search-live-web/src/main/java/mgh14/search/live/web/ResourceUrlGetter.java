package mgh14.search.live.web;

import java.net.URI;
import java.util.List;

/**
 * Interface for retrieving a list of URI resources
 */
public interface ResourceUrlGetter {

  void setSearchString(String searchString);

  List<URI> getResources();
}
