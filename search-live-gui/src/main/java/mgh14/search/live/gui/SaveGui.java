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

import mgh14.search.live.service.SaveController;

/**
 *
 */
public class SaveGui {

  private JFrame mainFrame;
  private JLabel statusLabel;
  private JPanel controlPanel;

  private SaveController controller;

  public SaveGui(SaveController controller) {
    this.controller = controller;

    prepareGui();
    createSaveButton();
  }

  private void prepareGui() {
    mainFrame = new JFrame("SearchLive Control Panel");
    mainFrame.setSize(200, 200);
    mainFrame.setLayout(new GridLayout(3, 1));
    mainFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent){
        //System.out.println("Finished");
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
    JButton okButton = new JButton("Save Current Image");

    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        statusLabel.setText("Image saved.");
        System.out.println("Saving current image...");
        final String fileSaved = controller.saveCurrentImage();
        if(fileSaved != null) {
          System.out.println("Image saved: [" + fileSaved + "]");
        }
      }
    });

    controlPanel.add(okButton);
    mainFrame.setVisible(true);
  }

}
