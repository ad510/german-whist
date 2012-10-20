// SelectInputPane.java
// Copyright (c) 2012 Andrew Downing
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** scoll pane (not panel) for user to choose from several buttons to click */
public class SelectInputPane extends JScrollPane implements ActionListener {
  private final Color backColor; /**< background color of panel */
  private final Insets insets; /**< padding around laid out components */
  private final ActionListener action; /**< reference to action to perform when button clicked */

  /** constructor for selection input pane */
  public SelectInputPane(ActionListener selectAction, Color newBackColor, Insets newInsets) {
    action = selectAction; // store reference to selection action
    backColor = newBackColor;
    insets = newInsets;
  }

  /** re-layout the pane to display specified instruction and choices */
  public void update(String instruction, ArrayList<String> choices) {
    GridBagConstraints gbc = new GridBagConstraints();
    // reset inner panel
    JPanel innerPanel = new JPanel();
    setViewportView(innerPanel);
    // start setting up layout and constraints
    innerPanel.setBackground(backColor);
    innerPanel.setLayout(new GridBagLayout());
    gbc.weightx = 0.5;
    gbc.insets = insets;
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
