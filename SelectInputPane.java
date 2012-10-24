//
// SelectInputPane.java
// author: Andrew Downing
//

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** scoll pane (not panel) for user to choose from several buttons to click */
public class SelectInputPane extends JScrollPane implements ActionListener {
  private ActionListener action; /**< reference to action to perform when button clicked */

  /** constructor for selection input pane */
  public SelectInputPane(ActionListener selectAction) {
    action = selectAction; // store reference to selection action
  }

  /** re-layout the pane to display specified instruction and choices */
  public void update(String instruction, ArrayList<String> choices) {
    GridBagConstraints gbc = new GridBagConstraints();
    // reset inner panel
    JPanel innerPanel = new JPanel();
    setViewportView(innerPanel);
    // start setting up layout and constraints
    innerPanel.setBackground(Application.BackColor);
    innerPanel.setLayout(new GridBagLayout());
    gbc.weightx = 0.5;
    gbc.insets = Application.StdInsets;
    gbc.gridx = 0;
    // add new components
    gbc.gridy = 0;
    innerPanel.add(new JLabel(instruction));
    for (int i = 0; i < choices.size(); i++) {
      JButton btnChoice = new JButton(choices.get(i));
      gbc.gridy = i + 1;
      innerPanel.add(btnChoice, gbc);
      btnChoice.addActionListener(this); // register button listener
    }
  }

  /** called when button clicked, and triggers new action event containing clicked button's text */
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() instanceof JButton) {
      action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ((JButton)ae.getSource()).getText()));
    }
  }
}
