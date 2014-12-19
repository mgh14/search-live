package mgh14.search.live.service;

/**
 *
 */
public class SaveController {

  ApplicationCycler cycler;

  public void setApplicationCycler(ApplicationCycler cycler) {
    this.cycler = cycler;
  }

  public void saveCurrentImage() {
    cycler.saveCurrentImage();
  }
}
