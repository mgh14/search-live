package mgh14.search.live.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import mgh14.search.live.gui.controller.GuiController;
import mgh14.search.live.model.web.util.FileUtils;
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
  private static final String COL_LAYOUT = "5px, center:pref, 10px, center:pref, 10px, " +
    "center:pref, 7px, 3px, center:pref, 10px, center:pref, 10px, center:pref, 5px";
  private static final String ROW_LAYOUT = "5px, center:pref, 7px, center:pref, 5px";
  private static final Dimension BUTTON_DIMENSION_OBJ = new Dimension(60, 35);
  private static final int BUTTON_MARGIN = 5;
  private static final int CLICK_PROCESS_BUTTON_DISABLE_DURATION = 500;  // milliseconds

  @Autowired
  private GuiController controller;
  @Autowired
  private ExecutorService executorService;
  @Autowired
  private FileUtils fileUtils;

  private JFrame mainFrame;
  private JLabel statusText;
  private JTextField queryText;

  // buttons
  private JButton startResourceCycleButton;
  private JButton saveCurrentResourceButton;
  private JButton pauseResourceCycleButton;
  private JButton resumeResourceCycleButton;
  private JButton nextResourceButton;
  private JButton deleteAllResourcesButton;
  private UIDefaults def;

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

    prepareGui();

    def = new UIDefaults();
    def.put("Button.contentMargins", new Insets(BUTTON_MARGIN,
      BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN));

    createButtons();
    mainFrame.revalidate();
  }

  @PostConstruct
  public void setIcons() {
    startResourceCycleButton.setIcon(getIcon("test-icon-small.png"));
    /*saveCurrentResourceButton.setIcon(getIcon(""));
    pauseResourceCycleButton.setIcon(getIcon(""));
    resumeResourceCycleButton.setIcon(getIcon(""));
    nextResourceButton.setIcon(getIcon(""));
    deleteAllResourcesButton.setIcon(getIcon(""));*/
  }

  public void setQueryText(String searchString) {
    if (searchString == null) {
      searchString = "";
    }
    queryText.setText(searchString);
  }

  public String setResourceSaveDirectory() {
    final JFileChooser fileChooser = new JFileChooser(fileUtils.getResourceDir());
    fileChooser.setDialogTitle("Choose the directory for saved images");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    int returnValue = fileChooser.showOpenDialog(mainFrame);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile().getAbsolutePath() +
        File.separator;
    }

    return null;
  }

  public void setStatusText(String newStatusText) {
    statusText.setText(newStatusText);
    /*executorService.execute(new Runnable() {
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
    });*/
  }

  public void setErrorStatusText(String newStatusText) {
    setStatusText("<html><font color=RED>" + newStatusText +
      "</font/></html>");
  }
  
  private void prepareGui() {
    setLookFeelAndTheme();

    // set up main frame
    mainFrame = new JFrame("SearchLive Control Panel");
    final Dimension mainFrameDimension = new Dimension(430, 115);
    mainFrame.setMinimumSize(mainFrameDimension);
    mainFrame.setMaximumSize(mainFrameDimension);
    mainFrame.setResizable(false);
    mainFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        setEnabledForAllButtons(false);
        controller.shutdownApplication();
      }
    });

    // set up JGoodies builder and layout
    //builder = new FormDebugPanel();
    final FormLayout layout = new FormLayout(COL_LAYOUT, ROW_LAYOUT);
    builder = new PanelBuilder(layout);
    //builder.setLayout(layout);
    layout.setColumnGroups(new int[][]{{2, 4, 6, 9, 11, 13}, {3, 5, 10, 12}});

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
    mainFrame.setVisible(true);
  }

  private void setLookFeelAndTheme() {
    try {
      UIManager.setLookAndFeel(new NimbusLookAndFeel());
    } catch (Exception e) {
      Log.error("Error setting user interface theme. " +
        "Setting default Java theme...\nStack trace: ", e);
    }
  }

  private void createButtons() {
    createStartButton();
    createSaveButton();
    createPauseButton();
    createResumeButton();
    createNextButton();
    createDeleteAllResourcesButton();
    refreshButtonsEnabled();
  }

  private Icon getIcon(String iconFilename) {
    final String iconsLocation = fileUtils.constructFilepathWithSeparator("C:",
      "Users", "mgh14", "search-live", "search-live-gui", "src", "main",
      "resources", "icons");
    try {
      return new ImageIcon(ImageIO.read(
        new FileInputStream(iconsLocation + iconFilename)));
    } catch (IOException e) {
      Log.error("Error fetching icon {}:", iconFilename, e);
    }

    return null;
  }

  private void createStartButton() {
    startResourceCycleButton = new JButton("St");
    startResourceCycleButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    startResourceCycleButton.setMinimumSize(BUTTON_DIMENSION_OBJ);
    startResourceCycleButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    startResourceCycleButton.putClientProperty("Nimbus.Overrides", def);

    // TODO: Handle case where cycle is stopped (e.g. for errors)
    startResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final String currentQueryText = queryText.getText();
        if (!currentSearchString.equals(currentQueryText)) {
          currentSearchString = currentQueryText;
          resourceCyclePaused.set(false);

          resourceCycleStarted.set(true);
          refreshQueryFieldEnabled();
          disableButtonsDuringButtonClickProcess();
          controller.startResourceCycle(currentQueryText);
          setStatusText("Resource cycle started");
        }
        else {
          Log.debug("Not starting due to same query entered: {} and {}",
            currentQueryText, currentSearchString);
        }
      }
    });

    builder.add(startResourceCycleButton, cellConstraints.xy(2, 4));
  }

  private void createSaveButton(){
    saveCurrentResourceButton = new JButton("Sv");
    saveCurrentResourceButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    saveCurrentResourceButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    saveCurrentResourceButton.setMinimumSize(BUTTON_DIMENSION_OBJ);
    saveCurrentResourceButton.putClientProperty("Nimbus.Overrides", def);

    saveCurrentResourceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshQueryFieldEnabled();
        disableButtonsDuringButtonClickProcess();
        controller.saveCurrentImage();
      }
    });

    builder.add(saveCurrentResourceButton, cellConstraints.xy(4, 4));
  }

  private void createPauseButton() {
    pauseResourceCycleButton = new JButton("Pa");
    pauseResourceCycleButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    pauseResourceCycleButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    pauseResourceCycleButton.setMinimumSize(BUTTON_DIMENSION_OBJ);
    pauseResourceCycleButton.putClientProperty("Nimbus.Overrides", def);

    pauseResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resourceCyclePaused.set(true);
        refreshQueryFieldEnabled();
        disableButtonsDuringButtonClickProcess();
        controller.pauseResourceCycle();

        // Note: this setText method is used instead of the setStatusText
        // method because the pause text should stay in the label until
        // cycling resumes
        // TODO: make this work with new GUI status field updating
        statusText.setText("Paused cycle");
      }
    });

    builder.add(pauseResourceCycleButton, cellConstraints.xy(6, 4));
  }

  private void createResumeButton() {
    resumeResourceCycleButton = new JButton("Re");
    resumeResourceCycleButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    resumeResourceCycleButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    resumeResourceCycleButton.setMinimumSize(BUTTON_DIMENSION_OBJ);
    resumeResourceCycleButton.putClientProperty("Nimbus.Overrides", def);

    resumeResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
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

    builder.add(resumeResourceCycleButton, cellConstraints.xy(9, 4));
  }

  private void createNextButton() {
    nextResourceButton = new JButton("Nx");
    nextResourceButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    nextResourceButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    nextResourceButton.setMinimumSize(BUTTON_DIMENSION_OBJ);
    nextResourceButton.putClientProperty("Nimbus.Overrides", def);

    nextResourceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshQueryFieldEnabled();
        disableButtonsDuringButtonClickProcess();
        controller.cycleNextResource();

        // Note: this setText method is used instead of the setStatusText
        // method because the skipping text should stay in the label until
        // the current resource has been skipped.
        // TODO: make this work with new GUI status field updating
        statusText.setText("Skipping current resource...");
      }
    });

    builder.add(nextResourceButton, cellConstraints.xy(11, 4));
  }

  private void createDeleteAllResourcesButton() {
    deleteAllResourcesButton = new JButton("Dl");
    deleteAllResourcesButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    deleteAllResourcesButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    deleteAllResourcesButton.setMinimumSize(BUTTON_DIMENSION_OBJ);
    deleteAllResourcesButton.putClientProperty("Nimbus.Overrides", def);

    deleteAllResourcesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshQueryFieldEnabled();
        disableButtonsDuringButtonClickProcess();
        controller.deleteAllResources();
        setStatusText("Resources deleted.");
      }
    });

    builder.add(deleteAllResourcesButton, cellConstraints.xy(13, 4));
  }

  private void disableButtonsDuringButtonClickProcess() {
    setEnabledForAllButtons(false);
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(CLICK_PROCESS_BUTTON_DISABLE_DURATION);
        }
        catch (InterruptedException e) {
          Log.info("Interrupt:", e);
        }

        refreshButtonsEnabled();
      }
    });
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

    deleteAllResourcesButton.setEnabled(true);
  }

  private void refreshButtonsEnabled() {
    Log.debug("Refreshing buttons enabled...");
    if (resourceCycleStarted.get() && !resourceCyclePaused.get()) {
      saveCurrentResourceButton.setEnabled(true);
      pauseResourceCycleButton.setEnabled(true);
      nextResourceButton.setEnabled(true);

      startResourceCycleButton.setEnabled(false);
      resumeResourceCycleButton.setEnabled(false);
    }
    else if (resourceCycleStarted.get() && resourceCyclePaused.get()) {
      startResourceCycleButton.setEnabled(true);
      resumeResourceCycleButton.setEnabled(true);

      saveCurrentResourceButton.setEnabled(false);
      pauseResourceCycleButton.setEnabled(false);
      nextResourceButton.setEnabled(false);
    }
    else if (!resourceCycleStarted.get() && !resourceCyclePaused.get()) {
      startResourceCycleButton.setEnabled(true);

      saveCurrentResourceButton.setEnabled(false);
      pauseResourceCycleButton.setEnabled(false);
      nextResourceButton.setEnabled(false);
      resumeResourceCycleButton.setEnabled(false);
    }
    else if (!resourceCycleStarted.get() && resourceCyclePaused.get()) {
      // this case should never happen
      Log.error("Invalid state reached--cycle not started and paused!");
    }

    deleteAllResourcesButton.setEnabled(true);
  }

  private void setEnabledForAllButtons(boolean enabled) {
    startResourceCycleButton.setEnabled(enabled);
    saveCurrentResourceButton.setEnabled(enabled);
    pauseResourceCycleButton.setEnabled(enabled);
    resumeResourceCycleButton.setEnabled(enabled);
    nextResourceButton.setEnabled(enabled);
    deleteAllResourcesButton.setEnabled(enabled);
  }

}
