package mgh14.search.live.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import mgh14.search.live.gui.button.ButtonManager;
import mgh14.search.live.gui.controller.CommandExecutorController;
import mgh14.search.live.gui.menu.MenuBarManager;
import mgh14.search.live.gui.menu.SecondsToWaitDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for constructing the SearchLive control panel GUI
 */
@Component
public class ControlPanel {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());
  private static final int SECONDS_BEFORE_LABEL_CLEAR = 15;
  private static final String COL_LAYOUT = "5px, center:pref, 10px, center:pref, " +
    "10px, center:pref, 7px, 3px, center:pref, 10px, center:pref, 10px, " +
    "center:pref, 5px";
  private static final String ROW_LAYOUT = "5px, center:pref, 7px, center:pref, 5px";

  @Autowired
  private CommandExecutorController controller;
  @Autowired
  private MenuBarManager menuBarManager;
  @Autowired
  private ButtonManager buttonManager;
  @Autowired
  private ExecutorService executorService;
  @Autowired
  private GuiUtils guiUtils;

  private JFrame mainFrame;
  private JLabel statusText;
  private JTextField queryText;

  private PanelBuilder builder;
  //private FormDebugPanel builder;
  private CellConstraints cellConstraints;

  private AtomicBoolean resourceCycleStarted;
  private AtomicBoolean resourceCyclePaused;
  private String currentSearchString;

  public ControlPanel() {
    this.cellConstraints = new CellConstraints();
    resourceCycleStarted = new AtomicBoolean(false);
    resourceCyclePaused = new AtomicBoolean(false);
    currentSearchString = "";

    setLookFeelAndTheme();
    prepareControlPanel();
  }

  public void setQueryText(String searchString) {
    if (searchString == null) {
      searchString = "";
    }
    queryText.setText(searchString);
  }

  public String getResourceSaveDirectory() {
    return guiUtils.chooseFileLocation(mainFrame);
  }

  public int getNewSecondsToSleep() {
    final SecondsToWaitDialog dialogue =
      new SecondsToWaitDialog(mainFrame);
    dialogue.setVisible(true);

    return dialogue.getResult();
  }

  public void setStatusText(String newStatusText) {
    Log.debug("Setting new status text: [{}]",
      newStatusText);
    statusText.setText(newStatusText);
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(SECONDS_BEFORE_LABEL_CLEAR * 1000);
        }
        catch (InterruptedException e) {
          Log.error("Interrupted exception: ", e);
        }

        statusText.setText("");
      }
    });
  }

  public void setErrorStatusText(String newStatusText) {
    setStatusText("<html><font color=RED>" + newStatusText +
      "</font/></html>");
  }

  // ------------------ Internal Methods ------------------//

  private void prepareControlPanel() {
    // set up main frame
    mainFrame = new JFrame("SearchLive Control Panel");
    final Dimension mainFrameDimension = new Dimension(430, 135);
    mainFrame.setMinimumSize(mainFrameDimension);
    mainFrame.setMaximumSize(mainFrameDimension);
    mainFrame.setResizable(false);
    mainFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        buttonManager.setEnabledForAllButtons(false);
        controller.shutdownApplication();
      }
    });

    // set up JGoodies builder and layout
    //builder = new FormDebugPanel();
    final FormLayout layout = new FormLayout(COL_LAYOUT, ROW_LAYOUT);
    builder = new PanelBuilder(layout);
    //builder.setLayout(layout);
    layout.setColumnGroups(new int[][]{{2, 4, 6, 9, 11, 13},
      {3, 5, 10, 12}});

    // build query text label
    final JLabel queryLabel = new JLabel("<html>" +
      "<b>Search:</b></html>");
    builder.add(queryLabel, cellConstraints.xy(2, 2));

    // build query text field
    this.queryText = new JTextField();
    builder.add(queryText, cellConstraints.xyw(3, 2, 5));

    // build status text label
    statusText = new JLabel("", JLabel.CENTER);
    builder.add(statusText, cellConstraints.xyw(9, 2, 5));

    // show main frame
    mainFrame.add(builder.getPanel());
    //mainFrame.add(builder);
    mainFrame.revalidate();
    mainFrame.setVisible(true);
  }

  @PostConstruct
  private void setMainFrameIcon() {
    mainFrame.setIconImage(guiUtils.getImageIcon("logo.png")
      .getImage());
    mainFrame.revalidate();
  }

  @PostConstruct
  private void addMenuBar() {
    menuBarManager.addMenuBarToFrame(mainFrame);
    mainFrame.revalidate();
  }

  @PostConstruct
  private void setButtonFunctions() {
    // start button
    JButton controlButton = getStartButton();
    // TODO: Handle case where cycle is stopped (e.g. for errors)
    controlButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final String currentQueryText = queryText.getText();
        if (!currentSearchString.equals(currentQueryText)) {
          setStatusText("Starting cycle...");

          currentSearchString = currentQueryText;
          resourceCyclePaused.set(false);

          resourceCycleStarted.set(true);
          refreshQueryFieldEnabled();
          disableButtonsDuringButtonClickProcess();
          controller.startResourceCycle(currentQueryText);
        }
        else {
          Log.debug("Not starting due to same query entered: {}",
            currentQueryText, currentSearchString);
        }
      }
    });
    builder.add(controlButton, cellConstraints.xy(2, 4));

    // save button
    controlButton = getSaveButton();
    controlButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setStatusText("Saving...");

        refreshQueryFieldEnabled();
        disableButtonsDuringButtonClickProcess();
        controller.saveCurrentImage();
      }
    });
    builder.add(controlButton, cellConstraints.xy(4, 4));

    // pause button
    controlButton = getPauseButton();
    controlButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setStatusText("Pausing cycle...");

        resourceCyclePaused.set(true);
        refreshQueryFieldEnabled();
        disableButtonsDuringButtonClickProcess();
        controller.pauseResourceCycle();
      }
    });
    builder.add(controlButton, cellConstraints.xy(6, 4));

    // resume button
    controlButton = getResumeButton();
    controlButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setStatusText("Resuming cycle...");

        resourceCyclePaused.set(false);
        refreshQueryFieldEnabled();
        disableButtonsDuringButtonClickProcess();

        // Replace query text with current search string
        // if the query text has changed and then 'resume'
        // is hit (instead of start)
        if (!currentSearchString.equals(queryText.getText())) {
          queryText.setText(currentSearchString);
        }

        controller.resumeResourceCycle();
      }
    });
    builder.add(controlButton, cellConstraints.xy(9, 4));

    // next button
    controlButton = getNextButton();
    controlButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        statusText.setText("Skipping current resource...");

        refreshQueryFieldEnabled();
        disableButtonsDuringButtonClickProcess();
        controller.cycleNextResource();
      }
    });
    builder.add(controlButton, cellConstraints.xy(11, 4));

    // delete resources button
    controlButton = getDeleteResourcesButton();
    controlButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setStatusText("Deleting resources...");

        refreshQueryFieldEnabled();
        disableButtonsDuringButtonClickProcess();
        controller.deleteAllResources();
      }
    });
    builder.add(controlButton, cellConstraints.xy(13, 4));

    refreshButtons();
    mainFrame.revalidate();
  }

  private void setLookFeelAndTheme() {
    try {
      UIManager.setLookAndFeel(new NimbusLookAndFeel());
    } catch (Exception e) {
      Log.error("Error setting user interface theme. " +
        "Setting default Java theme...\nStack trace: ", e);
    }
  }

  private void refreshQueryFieldEnabled() {
    Log.debug("Refreshing query field enabled...");
    if (resourceCycleStarted.get() && !resourceCyclePaused.get()) {
      queryText.setEnabled(false);
    }
    else if (resourceCycleStarted.get() && resourceCyclePaused.get()) {
      queryText.setEnabled(true);
    }
    else if (!resourceCycleStarted.get() && !resourceCyclePaused.get()) {
      queryText.setEnabled(true);
    }
    else if (!resourceCycleStarted.get() && resourceCyclePaused.get()) {
      // this case should never happen
      Log.error("Invalid state reached in refreshing query field: " +
        "cycle not started and paused!");
    }
  }

  private JButton getStartButton() {
    return buttonManager.getControlButton(
      GuiParamNames.START_BUTTON);
  }

  private JButton getSaveButton() {
    return buttonManager.getControlButton(
      GuiParamNames.SAVE_BUTTON);
  }

  private JButton getPauseButton() {
    return buttonManager.getControlButton(
      GuiParamNames.PAUSE_BUTTON);
  }

  private JButton getResumeButton() {
    return buttonManager.getControlButton(
      GuiParamNames.RESUME_BUTTON);
  }

  private JButton getNextButton() {
    return buttonManager.getControlButton(
      GuiParamNames.NEXT_BUTTON);
  }

  private JButton getDeleteResourcesButton() {
    return buttonManager.getControlButton(
      GuiParamNames.DELETE_RESOURCES_BUTTON);
  }

  private void refreshButtons() {
    buttonManager.refreshButtonsEnabled(
      resourceCycleStarted.get(),
      resourceCyclePaused.get());
  }

  private void disableButtonsDuringButtonClickProcess() {
    buttonManager.disableButtonsDuringButtonClickProcess(
      resourceCycleStarted.get(),
      resourceCyclePaused.get());
  }

}
