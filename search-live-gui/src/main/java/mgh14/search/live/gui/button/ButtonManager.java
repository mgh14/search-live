package mgh14.search.live.gui.button;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Observable;
import javax.annotation.PostConstruct;
import javax.swing.JButton;

import mgh14.search.live.gui.GuiParamNames;
import mgh14.search.live.gui.GuiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class created for managing the buttons in the
 * ControlPanel class.
 */
@Component
public class ButtonManager extends Observable {

  private final Logger Log = LoggerFactory.getLogger(
    getClass().getSimpleName());

  private static final Dimension BUTTON_DIMENSION_OBJ =
    new Dimension(60, 35);

  @Autowired
  private GuiUtils guiUtils;

  private HashMap<String, JButton> buttons;

  public ButtonManager() {
    buttons = new HashMap<String, JButton>();
    buttons.put(GuiParamNames.START_BUTTON,
      getNewControlButton());
    buttons.put(GuiParamNames.SAVE_BUTTON,
      getNewControlButton());
    buttons.put(GuiParamNames.PAUSE_BUTTON,
      getNewControlButton());
    buttons.put(GuiParamNames.RESUME_BUTTON,
      getNewControlButton());
    buttons.put(GuiParamNames.NEXT_BUTTON,
      getNewControlButton());
    buttons.put(GuiParamNames
        .DELETE_RESOURCES_BUTTON,
      getNewControlButton());
  }

  @PostConstruct
  public void setIcons() {
    getControlButton(GuiParamNames.START_BUTTON)
      .setIcon(guiUtils.getImageIcon("start.png"));
    getControlButton(GuiParamNames.SAVE_BUTTON)
      .setIcon(guiUtils.getImageIcon("save.png"));
    getControlButton(GuiParamNames.PAUSE_BUTTON)
      .setIcon(guiUtils.getImageIcon("pause.png"));
    getControlButton(GuiParamNames.RESUME_BUTTON)
      .setIcon(guiUtils.getImageIcon("resume.png"));
    getControlButton(GuiParamNames.NEXT_BUTTON)
      .setIcon(guiUtils.getImageIcon("next.png"));
    getControlButton(GuiParamNames.
      DELETE_RESOURCES_BUTTON).setIcon(guiUtils
      .getImageIcon("delete.png"));

    notifyObserversWithGuiUpdate();
  }

  public JButton getControlButton(String buttonName) {
    return (buttons.containsKey(buttonName) ?
      buttons.get(buttonName) : null);
  }

  public void setEnabledForAllButtons(boolean enabled) {
    for (String buttonName : buttons.keySet()) {
      getControlButton(buttonName).setEnabled(enabled);
    }
  }

  private JButton getNewControlButton() {
    final JButton jbutton = new JButton();
    jbutton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    jbutton.setMinimumSize(BUTTON_DIMENSION_OBJ);
    jbutton.setPreferredSize(BUTTON_DIMENSION_OBJ);

    return jbutton;
  }

  private void notifyObserversWithGuiUpdate() {
    setChanged();
    notifyObservers();
  }

}
