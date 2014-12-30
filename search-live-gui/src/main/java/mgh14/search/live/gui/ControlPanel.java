package mgh14.search.live.gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

  private JFrame mainFrame;
  private JLabel statusLabel;
  private JPanel controlPanel;

  @Autowired
  private GuiController controller;

  public ControlPanel() {
    this.controller = null;

    prepareGui();
    createSaveButton();
  }

  private void prepareGui() {
    mainFrame = new JFrame("SearchLive Control Panel");
    mainFrame.setSize(200, 240);
    mainFrame.setLayout(new GridLayout(3, 1));
    mainFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent){
        System.exit(0);
      }
    });

    statusLabel = new JLabel("",JLabel.CENTER);
    statusLabel.setSize(350,100);

    controlPanel = new JPanel();
    controlPanel.setLayout(new FlowLayout());

    mainFrame.add(controlPanel);
    mainFrame.add(statusLabel);
    mainFrame.setVisible(true);
  }

  private void createSaveButton(){
    JButton saveCurrentResourceButton = new JButton("Save Current Image");
    JButton pauseResourceCycleButton = new JButton("Pause Resource Cycle");

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

    pauseResourceCycleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        controller.pauseResourceCycle();
        setStatusLabel("Paused cycle");
      }
    });


    controlPanel.add(saveCurrentResourceButton);
    controlPanel.add(pauseResourceCycleButton);
    mainFrame.setVisible(true);
  }

  private void setStatusLabel(String statusText) {
    statusLabel.setText(statusText);
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(15 * 1000);
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }

        statusLabel.setText("");
      }
    }).start();
  }

}
