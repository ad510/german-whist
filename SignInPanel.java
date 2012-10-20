// SignInPanel.java
// Copyright (c) 2012 Andrew Downing
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** panel to sign in to (and possibly create new account on) server */
public class SignInPanel extends JPanel implements ActionListener {
  private JTextField txtServerAddress; /**< text box to enter server domain name or IP address */
  private JTextField txtPlayerName; /**< text box to enter player name */
  private JPasswordField txtPassword; /**< text box to enter password */
  private JButton btnSubmit; /**< button to sign in or create new player account */
  private JLabel lblActionMsg; /**< label for displaying message or error */
  private ActionListener action; /**< reference to action that sends the sign in message */
  private SignInMsg packet; /**< message to send to server to sign in */

  /** constructor for sign in panel */
  public SignInPanel(ActionListener submitAction, Color backColor, Insets insets) {
    packet = new SignInMsg();
    // start setting up layout and contraints
    GridBagConstraints gbc = new GridBagConstraints();
    setBackground(backColor);
    setLayout(new GridBagLayout());
    gbc.weightx = 0.5;
    gbc.insets = insets;
    // instantiate components
    txtServerAddress = new JTextField(20);
    txtPlayerName = new JTextField(20);
    txtPassword = new JPasswordField(20);
    btnSubmit = new JButton();
    lblActionMsg = new JLabel();
    // add components
    gbc.gridx = 0;
    gbc.gridy = 0;
    add(new JLabel("Server domain name or IP address"), gbc);
    gbc.gridy++;
    add(txtServerAddress, gbc);
    gbc.gridy++;
    add(new JLabel("Player Name"), gbc);
    gbc.gridy++;
    add(txtPlayerName, gbc);
    gbc.gridy++;
    add(new JLabel("Password"), gbc);
    gbc.gridy++;
    add(txtPassword, gbc);
    gbc.gridy++;
    add(btnSubmit, gbc);
    gbc.gridy++;
    add(lblActionMsg, gbc);
    // register button listener
    btnSubmit.addActionListener(this);
    // store reference to button action
    action = submitAction;
  }

  /** reset text fields and action message text */
  private void reset() {
    txtServerAddress.setText("");
    txtPlayerName.setText("");
    txtPassword.setText("");
    btnSubmit.setEnabled(true);
    setActionMsg("");
  }

  /** display specified action message, allowing HTML formatting */
  public void setActionMsg(String message) {
    lblActionMsg.setText("<html>" + message + "</html>");
  }

  /** display specified action message in red text, allowing HTML formatting */
  public void setActionError(String message) {
    lblActionMsg.setText("<html><span style='color: red'>" + message + "</span></html>");
  }

  /** set up panel to be used for sign in */
  public void initSignIn() {
    reset();
    btnSubmit.setText("Sign In");
    packet.newPlayer = false;
  }

  /** set up panel to be used for creating a new player */
  public void initNewPlayer() {
    reset();
    btnSubmit.setText("Create New Player");
    setActionMsg("Do not use a valuable password because<br>it will be sent to and stored on the server in plaintext.");
    packet.newPlayer = true;
  }

  /** when button clicked, validate sign in info and notify parent action listener */
  public void actionPerformed(ActionEvent ae) {
    // copy input to packet
    packet.playerName = txtPlayerName.getText();
    packet.password = new String(txtPassword.getPassword());
    // validate input
    if (txtServerAddress.getText().isEmpty()) {
      setActionError("Please enter a server address");
      return;
    }
    if (!PlayerData.isValidName(packet.playerName)) {
      if (packet.playerName.isEmpty()) {
        setActionError("Please enter a player name");
      }
      else {
        setActionError("Player name may only contain letters, numbers, or spaces");
      }
      return;
    }
    if (packet.password.isEmpty()) {
      setActionError("Please enter your password");
      return;
    }
    // trigger new action event
    setActionMsg("Signing in...");
    action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
  }

  /** getter for server address */
  public final String getServerAddress() {
    return txtServerAddress.getText();
  }

  /** getter for packet to send to server */
  public final SignInMsg getPacket() {
    return packet;
  }
}
