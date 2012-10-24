//
// TextInputPanel.java
// author: Andrew Downing
//

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** panel to receive text input from user */
public class TextInputPanel extends JPanel implements ActionListener {
  private JTextField textField; /**< input text field */
  private JButton button; /**< button to submit input */
  private JLabel lblActionMsg; /**< label for displaying message or error when button clicked */
  private ActionListener action; /**< reference to action to perform when button clicked */

  /** constructor for text input panel */
  public TextInputPanel(String instruction, String btnText, ActionListener btnAction) {
    // start setting up layout and contraints
    GridBagConstraints gbc = new GridBagConstraints();
    setBackground(Application.BackColor);
    setLayout(new GridBagLayout());
    gbc.weightx = 0.5;
    gbc.insets = Application.StdInsets;
    gbc.gridx = 0;
    // instantiate components
    textField = new JTextField(20);
    button = new JButton(btnText);
    lblActionMsg = new JLabel();
    // add components
    gbc.gridy = 0;
    add(new JLabel(instruction), gbc);
    gbc.gridy = 1;
    add(textField, gbc);
    gbc.gridy = 2;
    add(button, gbc);
    gbc.gridy = 3;
    add(lblActionMsg, gbc);
    // register button listener
    button.addActionListener(this);
    // store reference to button action
    action = btnAction;
  }

  /** reset text field and action message text */
  public void reset() {
    textField.setText("");
    lblActionMsg.setText("");
  }

  /** display specified action message, allowing HTML formatting */
  public void setActionMsg(String message) {
    lblActionMsg.setText("<html>" + message + "</html>");
  }

  /** display specified action message in red text, allowing HTML formatting */
  public void setActionError(String message) {
    lblActionMsg.setText("<html><span style='color: red'>" + message + "</span></html>");
  }

  /** called when button clicked, and triggers new action event containing clicked text field's text */
  public void actionPerformed(ActionEvent ae) {
    action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, textField.getText()));
    textField.setText("");
  }
}
