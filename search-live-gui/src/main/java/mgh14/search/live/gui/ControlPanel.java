package mgh14.search.live.gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.seaglasslookandfeel.SeaGlassLookAndFeel;
import mgh14.search.live.gui.controller.GuiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for constructing the SearchLive control panel GUI
 */
@Component
public class ControlPanel {

  private final Logger Log = LoggerFactory.getLogger(this.getClass());
  private static final int SECONDS_BEFORE_LABEL_CLEAR = 15;

  @Autowired
  private GuiController controller;
  @Autowired
  private ExecutorService executorService;

  private JFrame mainFrame;
  private JLabel statusLabel;
  private JPanel controlPanel;

  private JTextField queryText;

  public ControlPanel() {
    this.controller = null;

    prepareGui();

    createStartButton();
    createSaveButton();
    createPauseButton();
    createResumeButton();
    createNextButton();
    createDeleteAllResourcesButton();
    mainFrame.revalidate();
  }

  private void setLookFeelAndTheme() {
    try {
      UIManager.setLookAndFeel(new SeaGlassLookAndFeel());
    } catch (Exception e) {
      Log.error("Error setting user interface theme. " +
        "Setting default Java theme...\nStack trace: ", e);
    }

    // TODO: set theme
  }

  private void prepareGui() {
    setLookFeelAndTheme();

    mainFrame = new JFrame("SearchLive Control Panel");
    mainFrame.setSize(200, 250);
    mainFrame.setLayout(new FlowLayout());
    mainFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        controller.shutdownApplication();
      }
    });

    statusLabel = new JLabel("", JLabel.CENTER);
    statusLabel.setSize(350, 100);

    controlPanel = new JPanel();
    controlPanel.setLayout(new GridLayout(0, 1));

    queryText = new JTextField();
    queryText.setSize(100, 20);

    controlPanel.add(queryText);
    mainFrame.add(controlPanel);
    mainFrame.add(statusLabel);
    mainFrame.setVisible(true);
  }

  public void setSearchString(String searchString) {
    queryText.setText(searchString);
  }

  private void createStartButton() {
    JButton startResourceCycleButton = new JButton("Start Resource Cycle");

    startResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //controller.startResourceCycle(queryField.getText());
        setStatusLabel("Resource cycle started");
      }
    });

    controlPanel.add(startResourceCycleButton);
  }

  private void createSaveButton(){
    JButton saveCurrentResourceButton = new JButton("Save Current Image");

    saveCurrentResourceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.saveCurrentImage();
        setStatusLabel("Image saved");
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

    controlPanel.add(saveCurrentResourceButton);
  }

  private void createPauseButton() {
    JButton pauseResourceCycleButton = new JButton("Pause Resource Cycle");

    pauseResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.pauseResourceCycle();

        // Note: this setText method is used instead of the setStatusLabel
        // method because the pause text should stay in the label until
        // cycling resumes
        statusLabel.setText("Paused cycle");
      }
    });
    pauseResourceCycleButton.setEnabled(false);

    controlPanel.add(pauseResourceCycleButton);
  }

  private void createResumeButton() {
    JButton resumeResourceCycleButton = new JButton("Resume Resource Cycle");

    resumeResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.resumeResourceCycle();
        setStatusLabel("Resumed cycle");
      }
    });
    resumeResourceCycleButton.setEnabled(false);

    controlPanel.add(resumeResourceCycleButton);
  }

  private void createNextButton() {
    JButton nextResourceButton = new JButton("Next Resource");

    nextResourceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.cycleNextResource();
        setStatusLabel("Next resource retrieved.");
      }
    });
    nextResourceButton.setEnabled(false);

    controlPanel.add(nextResourceButton);
  }

  private void createDeleteAllResourcesButton() {
    JButton deleteAllResourcesButton = new JButton("Delete All Resources");

    deleteAllResourcesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.deleteAllResources();
        setStatusLabel("Resources deleted.");
      }
    });

    controlPanel.add(deleteAllResourcesButton);
  }

  private void setStatusLabel(String statusText) {
    statusLabel.setText(statusText);
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(SECONDS_BEFORE_LABEL_CLEAR * 1000);
        }
        catch (InterruptedException e) {
          Log.error("Interrupted exception: ", e);
        }

        statusLabel.setText("");
      }
    });
  }

}
