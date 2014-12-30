package mgh14.search.live.service;

/**
 *
 */
public class SaveController {

  ResourceCycler cycler;

  public void setApplicationCycler(ResourceCycler cycler) {
    this.cycler = cycler;
  }

  public String saveCurrentImage() {
    return cycler.saveCurrentImage();
  }
}
