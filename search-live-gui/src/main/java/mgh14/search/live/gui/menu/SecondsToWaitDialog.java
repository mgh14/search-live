package mgh14.search.live.gui.menu;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for setting the number of seconds to cycle
 * setting in the Control Panel settings. Pops up as
 * a dialog box.
 */
public class SecondsToWaitDialog extends JDialog
  implements PropertyChangeListener,
  ActionListener {

  private final Logger Log = LoggerFactory.getLogger(
    getClass().getSimpleName());

  private static final String INSTRUCTION_STRING =
    "Enter the number of seconds you want each image to\n" +
      "stay on the screen. Make sure the value is a positive\n" +
      "whole number.\n\n";
  private static final String CALCULATION_TIP_STRING =
    "Tip: for values greater than 60 seconds, calculate\n " +
      "the minutes or hours and use that value instead\n " +
      "(e.g. 60 * 5 = 300 seconds, which is 5 minutes)\n";
  private static final String INVALID_RESPONSE_DIALOG_TITLE =
    "Invalid Response";
  private static final String INVALID_RESPONSE_EXPLANATION =
    "Sorry, the number you have entered is invalid.\n Please " +
      "enter a positive whole number.";

  private JTextField textField;
  private JOptionPane optionPane;
  private int result;

  public SecondsToWaitDialog(Frame frame,
    int secondsToSleep) {
    super(frame, true);

    final int numAllowedTextChars = 10;
    textField = new JTextField(numAllowedTextChars);
    optionPane = new JOptionPane(
      new Object[]{INSTRUCTION_STRING,
        CALCULATION_TIP_STRING, "\n", textField},
      JOptionPane.PLAIN_MESSAGE,
      JOptionPane.OK_CANCEL_OPTION,
      null);
    textField.setText(String.valueOf(secondsToSleep));
    result = -1;

    setContentPane(optionPane);

    // Handle window closing correctly.
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    // Ensure the text field always gets the first focus.
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent ce) {
        textField.requestFocusInWindow();
      }
    });

    // Register an event handler that puts the text into
    // the option pane.
    textField.addActionListener(this);

    // Register an event handler that reacts to option
    // pane state changes.
    optionPane.addPropertyChangeListener(this);
    pack();
  }

  public int getResult() {
    return result;
  }

  /**
   * This method handles events for the text field.
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    optionPane.setValue(JOptionPane.OK_OPTION);
  }

  /**
   * This method reacts to state changes in the option pane.
   */
  @Override
  public void propertyChange(PropertyChangeEvent e) {
    String prop = e.getPropertyName();

    if (isVisible() &&
      (e.getSource() == optionPane) &&
      (JOptionPane.VALUE_PROPERTY.equals(prop) ||
        JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {

      final Object optionChosen = optionPane.getValue();
      if (optionChosen == JOptionPane.UNINITIALIZED_VALUE) {
        // ignore the reset change event
        return;
      }

      int optionChosenValue = (Integer) optionPane.getValue();

      // Reset the JOptionPane's value.
      // If not done, then if the user
      // presses the same button next time, no
      // property change event will be fired.
      optionPane.setValue(
        JOptionPane.UNINITIALIZED_VALUE);

      if (optionChosenValue == JOptionPane.OK_OPTION) {
        int result = getIntFromString(textField.getText());
        if (result >= 1) {
          Log.debug("successfully retrieved cycleSeconds result: " +
            "[{}]", result);
          this.result = result;
          dispose();
        }
        else {
          Log.error("Control panel cycleSeconds result is not " +
            "a positive integer: [{}]", result);
          JOptionPane.showMessageDialog(this,
            INVALID_RESPONSE_EXPLANATION,
            INVALID_RESPONSE_DIALOG_TITLE,
            JOptionPane.ERROR_MESSAGE);
          textField.requestFocusInWindow();
        }
      }
      else if (optionChosenValue == JOptionPane.CANCEL_OPTION) {
        // no op required
        Log.debug("Control panel cycleSeconds operation cancelled");
        dispose();
      }
    }
  }

  private int getIntFromString(String intString) {
    final int errorResult = -1;

    int result;
    try {
      result = Integer.parseInt(intString);
    } catch (NumberFormatException e) {
      return errorResult;
    }

    return (result > 0) ? result : errorResult;
  }
}
