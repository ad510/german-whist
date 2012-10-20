// PlayerStatsPane.java
// Copyright (c) 2012 Andrew Downing
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import java.util.*;
import java.awt.*;
import javax.swing.*;

/** scroll pane (not a panel) displaying statistics of existing players */
public class PlayerStatsPane extends JScrollPane {
  private final Color backColor; /**< background color of panel */
  private final Insets insets; /**< padding around laid out components */

  /** constructor for player statistics pane */
  public PlayerStatsPane(Color newBackColor, Insets newInsets) {
    backColor = newBackColor;
    insets = newInsets;
    update(new ArrayList<PlayerData>());
  }

  /** re-layout the pane to display updated player stats */
  public void update(ArrayList<PlayerData> players) {
    GridBagConstraints gbc = new GridBagConstraints();
    // reset inner panel
    JPanel innerPanel = new JPanel();
    setViewportView(innerPanel);
    // set up new layout
    // labels can be anonymous because entire panel is regenerated when it is updated
    innerPanel.setBackground(backColor);
    innerPanel.setLayout(new GridBagLayout());
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    gbc.insets = insets;
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
