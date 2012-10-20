// LobbyPanel.java
// Copyright (c) 2012 Andrew Downing
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** panel to preview upcoming game session as other players join */
public class LobbyPanel extends JPanel implements ActionListener {
  private ArrayList<JLabel> lblPlayers; /**< labels listing players who have joined the game */
  private JButton btnStartGame; /**< button to start the game for all players */
  private JButton btnLeaveGame; /**< button for this client to leave the game before it starts */
  private ActionListener action; /**< reference to action to perform when each button is clicked */

  /** constructor for lobby panel */
  public LobbyPanel(ActionListener lobbyAction, Color backColor, Insets insets) {
    // start setting up layout and contraints
    GridBagConstraints gbc = new GridBagConstraints();
    setBackground(backColor);
    setLayout(new GridBagLayout());
    gbc.weightx = 0.5;
    gbc.insets = insets;
    gbc.gridx = 0;
    gbc.gridy = 0;
    // instantiate and add components
    lblPlayers = new ArrayList<JLabel>();
    for (int i = 0; i < GamePanel.MaxPlayers; i++) {
      lblPlayers.add(new JLabel());
      add(lblPlayers.get(i), gbc);
      gbc.gridy++;
    }
    btnStartGame = new JButton("Start Game");
    btnStartGame.setEnabled(false);
    add(btnStartGame, gbc);
    gbc.gridy++;
    btnLeaveGame = new JButton("Leave Game");
    add(btnLeaveGame, gbc);
    // register button listeners
    btnStartGame.addActionListener(this);
    btnLeaveGame.addActionListener(this);
    // store reference to action listener
    action = lobbyAction;
  }

  /** update which players have joined the game */
  public void update(ArrayList<String> players) {
    for (int i = 0; i < lblPlayers.size(); i++) {
      if (i < players.size()) {
        lblPlayers.get(i).setText("Player " + (i + 1) + ": " + players.get(i));
      }
      else {
        lblPlayers.get(i).setText("");
      }
    }
    btnStartGame.setEnabled(players.size() >= GamePanel.MinPlayers && players.size() <= GamePanel.MaxPlayers);
  }

  /** pass button click events to parent action listener */
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == btnLeaveGame) {
      action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "leave"));
    }
    else if (ae.getSource() == btnStartGame) {
      action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "start"));
    }
  }
}
