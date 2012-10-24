// Application.java
// Copyright (c) 2012 Andrew Downing
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

/**
@mainpage German Whist card game

@section cardimg Note About Card Images

Extract Mr. Crowley's card images to the folder card_img/.

@section overview Overview

This Java program implements the card game German Whist as described at https://en.wikipedia.org/wiki/German_Whist and includes the capability to store player information persistently between sessions.

@section reqs Requirements

- Implement German Whist as described at https://en.wikipedia.org/wiki/German_Whist .
- Save player data on exit and load it at startup.
- Use menus for starting and stopping a game, exiting, creating players, and listing all players.
- Have a panel each for creating players, displaying player stats, selecting players to play a game, and playing the game.
- The program must use a GUI for all input and output. (Note that I occasionally outputted unrecoverable errors in the terminal.)

@section classes Classes

See the "Classes" tab at the top of the page.

@section globals Global Data/Functions

Java requires that all variables and functions are in a class, so there are no global variables or functions. (Am I misinterpreting this section?)

@section arch High-level Architecture

The Application class contains the code to handle menu items and switch between panels. The PlayerStatsPane class specifically displays player stats, but the SelectInputPane and TextInputPanel classes are more generically written. The GamePanel class handles initialization in its constructor and newGame(), handles player commands (and thus most rules of the game) in mouseClicked(), and handles drawing in paint(). The GamePanel class stores the cards in the talon and trick Decks, and each instance of the player class contains its own hand Deck. The Player class is essentially an intelligent wrapper around the Deck class specifically designed for handling the players' hands. The Deck class stores a collection of cards regardless if they are displayed together or spread out, and contains functions used by the GamePanel and Player classes to manipulate cards, e.g. shuffling, sorting, moving cards between decks, and drawing the deck. The Card class requires that images are loaded to its static variables backImg and frontImgs before it is used, and implements getters for rank, suit, and whether the card is face up. Only whether the card is face up can be changed after the Card constructor is called.

@section ui User Interface

In-game, the talon deck is at the upper right, the player's hands are spread out along the bottom, and a status display is at top center. The player hands are face up only if it is that player's turn to move. The user plays a card by clicking it, moving the card to the upper left. When all players have played a card, the winner of the trick is determined, the cards are removed from the upper left, and each player receives 1 card from the talon. When the game has ended, the winner is displayed and clicking the window starts a new game. (See https://en.wikipedia.org/wiki/German_Whist for more detailed game rules.) Outside the game, the file menu handles creating new games, switching to the active game, and ending the game. The player menu gives access to panels to view and edit player info.

@section testing Test Cases

In addition to those from homework 1, cases to test for include entering an empty or invalid player name, having too few players to be able to start a game, attempting to resume a game when no game has been started, the player list not fitting in the window, and errors saving and loading from file. Nominal test cases include adding and deleting (valid) players, deleting players, viewing player stats, and starting a game. I tested all of the test cases listed above (and more), and checked that the game rules were still implemented correctly since I refactored some of that code.
*/

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

/** application window containing menus and top-level panels/panes */
public class Application extends JFrame implements ActionListener, MenuListener {
  public static final Color BackColor = new Color(255, 192, 128); /**< background color of panels */
  public static final Insets StdInsets = new Insets(10, 10, 10, 10); /**< standard padding around laid out components */
  private static final String SettingsPath = "save/players.dat"; /**< file path of player data file */

  // menus
  private JMenuBar menuBar;
  private JMenu menuFile;
  private JMenuItem menuNewGame;
  /* I considered implementing loading and saving games, but decided against it because:
     (1) syncing between saved games and player data file is difficult
     (2) this will be even more difficult after adding networking */
  //private JMenuItem menuLoadGame;
  //private JMenuItem menuSaveGame;
  private JMenuItem menuContinueGame;
  private JMenuItem menuStopGame;
  private JMenuItem menuExit;
  private JMenu menuPlayers;
  private JMenuItem menuNewPlayer;
  private JMenuItem menuDeletePlayer;
  private JMenuItem menuPlayerStats;

  /** layout of the window, containing the different application panels */
  private CardLayout layout;
  /** name of the current panel or pane being displayed */
  private String currentPanel;

  // panels
  private TextInputPanel panelNewPlayer; /**< panel to input new player names */
  private SelectInputPane paneDeletePlayer; /**< scroll pane to choose players to delete */
  private PlayerStatsPane panePlayerStats; /**< scroll pane to display player statistics */
  private SelectInputPane paneSelectPlayers; /**< scroll pane to select players to play a game */
  private GamePanel panelGame; /**< panel to play German Whist */

  /** ArrayList of all players (including those not playing) */
  private ArrayList<Player> players;
  /** ArrayList of players selected so far for a new game */
  private ArrayList<Player> selectedPlayers;
  /** whether should start game as soon as there's enough players to choose from */
  private boolean startGameASAP;

  /** constructor for game window */
  public Application() {
    int i, j;
    // set up window
    setTitle("German Whist");
    setSize(640, 500);
    setLocationRelativeTo(null); // center window on screen
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit program when window is closed
    addWindowListener(new AppCloseListener());
    // load players from file
    loadSettings();
    // instantiate menus
    menuBar = new JMenuBar();
    menuFile = new JMenu("File");
    menuNewGame = new JMenuItem("New Game");
    //menuLoadGame = new JMenuItem("Load Game");
    //menuSaveGame = new JMenuItem("Save Game");
    menuContinueGame = new JMenuItem("Continue Game");
    menuStopGame = new JMenuItem("Stop Game");
    menuExit = new JMenuItem("Exit");
    menuPlayers = new JMenu("Players");
    menuNewPlayer = new JMenuItem("Add New Players");
    menuDeletePlayer = new JMenuItem("Delete Player");
    menuPlayerStats = new JMenuItem("Player Stats");
    // add menus
    menuFile.add(menuNewGame);
    //menuFile.add(menuLoadGame);
    //menuFile.add(menuSaveGame);
    menuFile.addSeparator();
    menuFile.add(menuContinueGame);
    menuFile.add(menuStopGame);
    menuFile.addSeparator();
    menuFile.add(menuExit);
    menuPlayers.add(menuNewPlayer);
    menuPlayers.add(menuDeletePlayer);
    menuPlayers.addSeparator();
    menuPlayers.add(menuPlayerStats);
    menuBar.add(menuFile);
    menuBar.add(menuPlayers);
    setJMenuBar(menuBar);
    // register menu item listeners
    for (i = 0; i < menuBar.getMenuCount(); i++) {
      for (j = 0; j < menuBar.getMenu(i).getItemCount(); j++) {
        if (menuBar.getMenu(i).getItem(j) instanceof JMenuItem) { // skip separators
          menuBar.getMenu(i).getItem(j).addActionListener(this);
        }
      }
    }
    menuFile.addMenuListener(this); // to update whether items are enabled when menu is clicked
    // set up card layout
    layout = new CardLayout();
    setLayout(layout);
    // instantiate panels and panes
    panelNewPlayer = new TextInputPanel("Enter name of new player", "Add Player", this);
    paneDeletePlayer = new SelectInputPane(this);
    panePlayerStats = new PlayerStatsPane(players);
    paneSelectPlayers = new SelectInputPane(this);
    panelGame = new GamePanel();
    // add panels
    add(panelNewPlayer, "new player");
    add(paneDeletePlayer, "delete player");
    add(panePlayerStats, "player stats");
    add(paneSelectPlayers, "select players");
    add(panelGame, "game");
    // start new game
    setVisible(true); // display window
    startGame();
  }

  public static void main(String[] args) {
    Application app = new Application();
  }

  /** load player data from file */
  @SuppressWarnings("unchecked") // casting from Object to ArrayList<Players> causes an unavoidable warning
  private void loadSettings() {
    try {
      ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(SettingsPath));
      players = (ArrayList<Player>)inStream.readObject();
      inStream.close();
    }
    catch (FileNotFoundException ex) {
      players = new ArrayList<Player>();
    }
    catch (Exception ex) {
      System.out.println("Error loading player data from file. Using empty player list.");
      players = new ArrayList<Player>();
    }
  }

  /** save player data to file
      (this ends any currently playing game before saving the file,
       so should only be called when the program exits) */
  private void saveSettings() {
    panelGame.stopGame();
    try {
      ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(SettingsPath));
      outStream.writeObject(players);
      outStream.close();
    }
    catch (Exception ex) {
      System.out.println("Error saving player data to file.");
      System.out.println("Make sure the \"save\" folder exists.");
    }
  }

  /** handle menu or button click */
  public void actionPerformed(ActionEvent ae) {
    Object src = ae.getSource();
    int i;
    // file menu
    if (src == menuNewGame) { // select players for new game
      startGame();
    }
    else if (src == menuContinueGame) { // continue game that has been started
      if (!panelGame.getGameOver()) {
        showPanel("game");
      }
    }
    else if (src == menuStopGame) { // end game in its current state
      panelGame.stopGame();
      if (currentPanel.equals("game")) {
        showPanel("player stats"); // game panel should not be displayed when a game is not being played
      }
    }
    else if (src == menuExit) { // exit program
      // call window closing events of window listeners
      WindowListener[] windowListeners = getWindowListeners();
      for (i = 0; i < windowListeners.length; i++) {
        windowListeners[i].windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
      }
      // exit program
      System.exit(0);
    }
    // players menu
    else if (src == menuNewPlayer) { // show panel to add new player
      showPanel("new player");
    }
    else if (src == menuDeletePlayer) { // show panel to delete player
      showPanel("delete player");
    }
    else if (src == menuPlayerStats) { // show player stats panel
      showPanel("player stats");
    }
    // new player panel
    else if (src == panelNewPlayer) {
      addPlayer(ae.getActionCommand());
    }
    // delete player panel
    else if (src == paneDeletePlayer) { // delete selected player
      String name = ae.getActionCommand();
      for (i = 0; i < players.size(); i++) {
        if (name.equals(players.get(i).getName())) {
          players.remove(i);
          break;
        }
      }
      showPanel("player stats");
    }
    // select playing players pane
    else if (src == paneSelectPlayers) {
      selectNewGamePlayer(ae.getActionCommand());
    }
    // unhandled
    else {
      System.out.println("TODO: handle actionPerformed of " + src.toString());
    }
  }

  /** update whether continue/stop game items are enabled when file menu clicked */
  public void menuSelected(MenuEvent e) {
    if (e.getSource() == menuFile) {
      boolean enable = !panelGame.getGameOver();
      menuContinueGame.setEnabled(enable);
      menuStopGame.setEnabled(enable);
    }
  }

  public void menuDeselected(MenuEvent e) {
  }

  public void menuCanceled(MenuEvent e) {
  }

  /** show panel (or pane) of specified name */
  private void showPanel(String panelName) {
    int i, j;
    if (panelName.equals("new player")) {
      panelNewPlayer.reset();
      startGameASAP = false;
    }
    else if (panelName.equals("delete player")) {
      paneDeletePlayer.update("Click player to delete", getPlayerNames());
    }
    else if (panelName.equals("player stats")) {
      panePlayerStats.update();
    }
    else if (panelName.equals("select players")) {
      ArrayList<String> names = getPlayerNames();
      // don't show players that have already been chosen
      for (i = 0; i < names.size(); i++) {
        for (j = 0; j < selectedPlayers.size(); j++) {
          if (names.get(i).equals(selectedPlayers.get(j).getName())) {
            names.remove(i);
            i--;
            break;
          }
        }
      }
      // update select player pane
      paneSelectPlayers.update("Select player " + (selectedPlayers.size() + 1) + " for this game", names);
    }
    else if (panelName.equals("game")) {
      if (panelGame.getGameOver()) {
        throw new IllegalStateException("A game has not been started");
      }
    }
    else {
      throw new IllegalArgumentException("There is no panel or pane called \"" + panelName + "\"");
    }
    layout.show(this.getContentPane(), panelName);
    currentPanel = panelName;
  }

  /** handle user request to start new game */
  private void startGame() {
    if (players.size() >= GamePanel.NPlayers) {
      // enough players to choose from, so show panel to select players
      selectedPlayers = new ArrayList<Player>();
      showPanel("select players");
    }
    else {
      // not enough players to choose from, so show panel to add new players
      showPanel("new player");
      startGameASAP = true;
    }
  }

  /** handle user request to add new player */
  private void addPlayer(String name) {
    // check that player name contains valid characters
    if (!isValidName(name)) {
      if (name.isEmpty()) {
        panelNewPlayer.setActionError("Please enter a player name");
      }
      else {
        panelNewPlayer.setActionError("Player name may only contain letters, numbers, or spaces");
      }
      return;
    }
    // check that new player name is unique
    for (int i = 0; i < players.size(); i++) {
      if (name.equalsIgnoreCase(players.get(i).getName())) {
        panelNewPlayer.setActionError("A player named \"" + players.get(i).getName() + "\" already exists");
        return;
      }
    }
    // add new player with specified name
    players.add(new Player(name, 0, 0));
    panelNewPlayer.setActionMsg("Player \"" + name + "\" added");
    if (startGameASAP && players.size() >= GamePanel.NPlayers) {
      // asked to start game as soon as possible and there are enough players,
      // so start new game
      startGame();
    }
  }

  /** handle user request to select player for a new game */
  private void selectNewGamePlayer(String name) {
    // add selected player to list
    for (int i = 0; i < players.size(); i++) {
      if (name.equals(players.get(i).getName())) {
        selectedPlayers.add(players.get(i));
        break;
      }
    }
    if (selectedPlayers.size() >= GamePanel.NPlayers) {
      // enough players have been selected, start new game
      // if too many players selected then GamePanel will throw an exception, but this shouldn't happen
      panelGame.newGame(selectedPlayers);
      selectedPlayers.clear();
      showPanel("game");
    }
    else {
      // not enough players have been selected, select another player
      showPanel("select players");
    }
  }

  /** returns ArrayList of names of all players */
  private final ArrayList<String> getPlayerNames() {
    ArrayList<String> ret = new ArrayList<String>();
    for (int i = 0; i < players.size(); i++) {
      ret.add(players.get(i).getName());
    }
    return ret;
  }

  /** class to handle window close event */
  private class AppCloseListener extends WindowAdapter {
    /** save player data on window exit */
    public void windowClosing(WindowEvent e) {
      saveSettings();
    }
  }

  /** returns whether string is not empty and is composed only of letters, numbers, or spaces
      (can be used to check player name, file name, etc) */
  public static boolean isValidName(String name) {
    if (name.isEmpty()) {
      return false;
    }
    for (char ch : name.toCharArray()) {
      if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch)) {
        return false;
      }
    }
    return true;
  }
}
