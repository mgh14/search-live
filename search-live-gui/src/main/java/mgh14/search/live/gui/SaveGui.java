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
  private JLabel headerLabel;
  private JLabel statusLabel;
  private JPanel controlPanel;

  private SaveController controller;

  public SaveGui(SaveController controller) {
    this.controller = controller;

    prepareGui();
    createSaveButton();
  }

  //public void set

  private void prepareGui() {
    mainFrame = new JFrame("Java Swing Examples");
    mainFrame.setSize(200, 200);
    mainFrame.setLayout(new GridLayout(3, 1));
    mainFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent){
        //System.out.println("Finished");
      }
    });
    headerLabel = new JLabel("abc", JLabel.CENTER);
    statusLabel = new JLabel("def",JLabel.CENTER);

    statusLabel.setSize(350,100);

    controlPanel = new JPanel();
    controlPanel.setLayout(new FlowLayout());

    mainFrame.add(headerLabel);
    mainFrame.add(controlPanel);
    mainFrame.add(statusLabel);
    mainFrame.setVisible(true);
  }

  private void createSaveButton(){
    headerLabel.setText("Control in action: Button");

    JButton okButton = new JButton("OK");

    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        statusLabel.setText("Ok Button clicked.");
        System.out.println("Saving current image...");
        controller.saveCurrentImage();
      }
    });

    controlPanel.add(okButton);
    mainFrame.setVisible(true);
  }

}