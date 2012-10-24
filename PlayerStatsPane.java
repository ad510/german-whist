//
// PlayerStatsPane.java
// author: Andrew Downing
//

import java.util.*;
import java.awt.*;
import javax.swing.*;

/** scroll pane (not a panel) displaying statistics of existing players */
public class PlayerStatsPane extends JScrollPane {
  private final ArrayList<Player> players; /**< reference to instance of player list */

  /** constructor for player statistics pane */
  public PlayerStatsPane(ArrayList<Player> playerList) {
    players = playerList;
  }

  /** re-layout the pane to display updated player stats */
  public void update() {
    GridBagConstraints gbc = new GridBagConstraints();
    // reset inner panel
    JPanel innerPanel = new JPanel();
    setViewportView(innerPanel);
    // set up new layout
    // labels can be anonymous because entire panel is regenerated when it is updated
    innerPanel.setBackground(Application.BackColor);
    innerPanel.setLayout(new GridBagLayout());
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    gbc.insets = Application.StdInsets;
    gbc.gridy = 0;
    gbc.gridx = 0;
    innerPanel.add(new JLabel("<html><span style='font-size: 16pt'>Player Name</span></html>"), gbc);
    gbc.gridx = 1;
    innerPanel.add(new JLabel("<html><span style='font-size: 16pt'># Games Won/Played</span></html>"), gbc);
    for (int i = 0; i < players.size(); i++) {
      gbc.gridy = i + 1;
      gbc.gridx = 0;
      innerPanel.add(new JLabel(players.get(i).getName()), gbc);
      gbc.gridx = 1;
      innerPanel.add(new JLabel(players.get(i).getGamesWon() + " out of " + players.get(i).getGamesPlayed()), gbc);
    }
  }
}
