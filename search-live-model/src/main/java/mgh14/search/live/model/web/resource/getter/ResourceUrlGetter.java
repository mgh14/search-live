package mgh14.search.live.model.web.resource.getter;

import java.net.URI;
import java.util.List;

/**
 * Interface for retrieving a list of URI resources
 */
public interface ResourceUrlGetter {

  void setSearchString(String searchString);

  List<URI> getResources();

  int getNumPagesRetrieved();
}
