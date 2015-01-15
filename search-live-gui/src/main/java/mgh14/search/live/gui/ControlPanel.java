package mgh14.search.live.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.jgoodies.forms.debug.FormDebugPanel;
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
    "center:pref, 10px, center:pref, 10px, center:pref, 10px, center:pref, 5px";
  private static final String ROW_LAYOUT = "center:pref, 7px, center:pref";
  private static final Dimension BUTTON_DIMENSION_OBJ = new Dimension(70, 40);

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

  private FormDebugPanel builder;
  private CellConstraints cellConstraints;

  public ControlPanel() {
    this.cellConstraints = new CellConstraints();

    prepareGui();

    createButtons();
    mainFrame.revalidate();
  }

  @PostConstruct
  public void setIcons() {
    startResourceCycleButton.setIcon(getIcon("test-icon.png"));
    /*saveCurrentResourceButton.setIcon(getIcon(""));
    pauseResourceCycleButton.setIcon(getIcon(""));
    resumeResourceCycleButton.setIcon(getIcon(""));
    nextResourceButton.setIcon(getIcon(""));
    deleteAllResourcesButton.setIcon(getIcon(""));*/
  }

  public void setSearchString(String searchString) {
    queryText.setText(searchString);
  }

  public String setResourceSaveDirectory() {
    final JFileChooser fileChooser = new JFileChooser(fileUtils.getResourceFolder());
    fileChooser.setDialogTitle("Choose the directory for saved images");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    int returnValue = fileChooser.showOpenDialog(mainFrame);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile().getAbsolutePath();
    }

    return null;
  }

  public void setStatusText(String newStatusText) {
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
  
  private void prepareGui() {
    setLookFeelAndTheme();

    // set up main frame
    mainFrame = new JFrame("SearchLive Control Panel");
    final Dimension mainFrameDimension = new Dimension(500, 115);
    mainFrame.setMinimumSize(mainFrameDimension);
    mainFrame.setMaximumSize(mainFrameDimension);
    mainFrame.setResizable(false);
    mainFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        controller.shutdownApplication();
      }
    });

    // set up JGoodies builder and layout
    builder = new FormDebugPanel();
    final FormLayout layout = new FormLayout(COL_LAYOUT, ROW_LAYOUT);
    builder.setLayout(layout);
    layout.setColumnGroups(new int[][]{{2, 4, 6, 8, 10, 12}, {1, 13}, {3, 5, 7, 9, 11}});
    //PanelBuilder builder = new PanelBuilder(layout);

    // build query text label
    final JLabel queryLabel = new JLabel("<html><font color=RED>" +
      "<b>Query:</b></font></html>");
    builder.add(queryLabel, cellConstraints.xy(2, 1));

    // build query text field
    this.queryText = new JTextField();
    builder.add(queryText, cellConstraints.xyw(4, 1, 4));

    // build status label
    final JLabel statusLabel = new JLabel("<html><font color=RED>" +
      "<b>Status:</b></html>");
    builder.add(statusLabel, cellConstraints.xy(8, 1));

    // build status text label
    statusText = new JLabel("", JLabel.CENTER);
    builder.add(statusText, cellConstraints.xyw(9, 1, 4));

    // show main frame
    mainFrame.add(builder);
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

    startResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.startResourceCycle(queryText.getText());
        setStatusText("Resource cycle started");
      }
    });

    builder.add(startResourceCycleButton, cellConstraints.xy(2, 3));
  }

  private void createSaveButton(){
    saveCurrentResourceButton = new JButton("Sv");
    saveCurrentResourceButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    saveCurrentResourceButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    saveCurrentResourceButton.setMinimumSize(BUTTON_DIMENSION_OBJ);

    saveCurrentResourceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.saveCurrentImage();
        setStatusText("Image saved");
        /*final String fileSaved = controller.saveCurrentImage();
        Log.info("Saving current image [{}]...", fileSaved);
        if(fileSaved != null && !fileSaved.isEmpty() && !fileSaved.equals("null")) {
          Log.info("Image saved: [{}]", fileSaved);
        }
        else {
          Log.error("Image couldnt be saved: [{}]", fileSaved);
        }*/
      }
    });
    saveCurrentResourceButton.setEnabled(false);

    builder.add(saveCurrentResourceButton, cellConstraints.xy(4, 3));
  }

  private void createPauseButton() {
    pauseResourceCycleButton = new JButton("Pa");
    pauseResourceCycleButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    pauseResourceCycleButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    pauseResourceCycleButton.setMinimumSize(BUTTON_DIMENSION_OBJ);

    pauseResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.pauseResourceCycle();

        // Note: this setText method is used instead of the setStatusText
        // method because the pause text should stay in the label until
        // cycling resumes
        statusText.setText("Paused cycle");
      }
    });
    pauseResourceCycleButton.setEnabled(false);

    builder.add(pauseResourceCycleButton, cellConstraints.xy(6, 3));
  }

  private void createResumeButton() {
    resumeResourceCycleButton = new JButton("Re");
    resumeResourceCycleButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    resumeResourceCycleButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    resumeResourceCycleButton.setMinimumSize(BUTTON_DIMENSION_OBJ);

    resumeResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.resumeResourceCycle();
        setStatusText("Resumed cycle");
      }
    });
    resumeResourceCycleButton.setEnabled(false);

    builder.add(resumeResourceCycleButton, cellConstraints.xy(8, 3));
  }

  private void createNextButton() {
    nextResourceButton = new JButton("Nx");
    nextResourceButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    nextResourceButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    nextResourceButton.setMinimumSize(BUTTON_DIMENSION_OBJ);

    nextResourceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.cycleNextResource();
        setStatusText("Next resource retrieved.");
      }
    });
    nextResourceButton.setEnabled(false);

    builder.add(nextResourceButton, cellConstraints.xy(10, 3));
  }

  private void createDeleteAllResourcesButton() {
    deleteAllResourcesButton = new JButton("Dl");
    deleteAllResourcesButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    deleteAllResourcesButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    deleteAllResourcesButton.setMinimumSize(BUTTON_DIMENSION_OBJ);

    deleteAllResourcesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.deleteAllResources();
        setStatusText("Resources deleted.");
      }
    });

    builder.add(deleteAllResourcesButton, cellConstraints.xy(12, 3));
  }

}
