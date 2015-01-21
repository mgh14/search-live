package mgh14.search.live.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.annotation.PostConstruct;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Class for constructing the SearchLive control panel GUI
 */
@Component
public class FormTest {

  private final Logger Log = LoggerFactory.getLogger(getClass().getSimpleName());
  private static final String COL_LAYOUT = "5px, center:pref, 10px, center:pref, 10px, " +
    "center:pref, 7px, 3px, center:pref, 10px, center:pref, 10px, center:pref, 5px";
  private static final String ROW_LAYOUT = "5px, center:pref, 7px, center:pref, 5px";

  private static final Dimension BUTTON_DIMENSION_OBJ = new Dimension(60, 35);

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

  private PanelBuilder builder;
  //private FormDebugPanel builder;
  private CellConstraints cellConstraints;

  public FormTest() {
    this.cellConstraints = new CellConstraints();
    prepareGui();
    createButtons();
    mainFrame.revalidate();
  }

  @PostConstruct
  public void setIcons() {
    //startResourceCycleButton.setIcon(getIcon("test-icon.png"));
    /*saveCurrentResourceButton.setIcon(getIcon(""));
    pauseResourceCycleButton.setIcon(getIcon(""));
    resumeResourceCycleButton.setIcon(getIcon(""));
    nextResourceButton.setIcon(getIcon(""));
    deleteAllResourcesButton.setIcon(getIcon(""));*/
  }

  public static void main(String... args) {
    new FormTest();
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

  private void prepareGui() {
    setLookFeelAndTheme();

    // set up main frame
    mainFrame = new JFrame("SearchLive Control Panel");
    final Dimension mainFrameDimension = new Dimension(440, 130);
    mainFrame.setMinimumSize(mainFrameDimension);
    //mainFrame.setMaximumSize(mainFrameDimension);
    //mainFrame.setResizable(false);
    mainFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        System.exit(-1);
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
  }

  private Icon getIcon(String iconFilename) {
    /*final String iconsLocation = fileUtils.constructFilepathWithSeparator("C:",
      "Users", "mgh14", "search-live", "search-live-gui", "src", "main",
      "resources", "icons");
    try {
      return new ImageIcon(ImageIO.read(
        new FileInputStream(iconsLocation + iconFilename)));
    } catch (IOException e) {
      Log.error("Error fetching icon {}:", iconFilename, e);
    }*/

    return null;
  }

  private void createStartButton() {
    startResourceCycleButton = new JButton("St");
    startResourceCycleButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    startResourceCycleButton.setMinimumSize(BUTTON_DIMENSION_OBJ);
    startResourceCycleButton.setPreferredSize(BUTTON_DIMENSION_OBJ);

    // TODO: Handle case where cycle is stopped (e.g. for errors)
    startResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setStatusText("Resource cycle started");
      }
    });

    builder.add(startResourceCycleButton, cellConstraints.xy(2, 4));
  }

  private void createSaveButton(){
    saveCurrentResourceButton = new JButton("Sv");
    saveCurrentResourceButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    saveCurrentResourceButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    saveCurrentResourceButton.setMinimumSize(BUTTON_DIMENSION_OBJ);

    saveCurrentResourceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Note: this setText method is used instead of the setStatusText
        // method because the pause text should stay in the label until
        // cycling resumes
        // TODO: make this work with new GUI status field updating
        statusText.setText("Saved: rsrc-" +
          System.currentTimeMillis() + ".jpg");
      }
    });

    builder.add(saveCurrentResourceButton, cellConstraints.xy(4, 4));
  }

  private void createPauseButton() {
    pauseResourceCycleButton = new JButton("Pa");
    pauseResourceCycleButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    pauseResourceCycleButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    pauseResourceCycleButton.setMinimumSize(BUTTON_DIMENSION_OBJ);

    pauseResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
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

    builder.add(resumeResourceCycleButton, cellConstraints.xy(9, 4));
  }

  private void createNextButton() {
    nextResourceButton = new JButton("Nx");
    nextResourceButton.setPreferredSize(BUTTON_DIMENSION_OBJ);
    nextResourceButton.setMaximumSize(BUTTON_DIMENSION_OBJ);
    nextResourceButton.setMinimumSize(BUTTON_DIMENSION_OBJ);

    nextResourceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
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

    deleteAllResourcesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setStatusText("Resources deleted.");
      }
    });

    builder.add(deleteAllResourcesButton, cellConstraints.xy(13, 4));
  }

}
