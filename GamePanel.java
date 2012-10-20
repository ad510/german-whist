import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/** panel that implements the rules of the card game German Whist */
public class GamePanel extends JPanel {
  public static final int Padding = 30; /**< number of pixels between items on the screen */
  public static final int TextHeight = 20; /**< approximate height of default drawString() font (in pixels) */
  public static final int MinPlayers = 2; /**< minimum number of allowed players
                                               (German Whist is a 2-player game, though it can be easily modified to handle more than 2 players) */
  public static final int MaxPlayers = 4; /**< maximum number of allowed players */
  public static final int NDealtCards = 13; /**< number of cards dealt to each player */

  private final Color backColor; /**< background color of panel */
  private Random rand; /**< random number generator with same seed for all clients playing this game */
  private PlayerSocket socket; /**< connection to server */
  protected int clientPlayer; /**< player ID of this client */
  protected Deck talon; /**< deck containing undealt cards */
  protected Deck trick; /**< deck in which tricks are played */
  protected ArrayList<Player> players; /**< ArrayList of currently playing players and their hand decks */
  protected Card.Suit trump; /**< suit that outranks all other suits during this game */
  protected int prevWinner; /**< ID of winner of previous trick */
  private int finalWinner; /**< ID of winner of the entire game */
  private boolean finalWinnerTie; /**< whether the game was a draw */
  protected boolean gameOver; /**< whether the game has ended */
  private String errorMsg; /**< message displayed to user if there is a problem */

  /** constructor to set up game for the first time */
  public GamePanel(Color newBackColor) {
    backColor = newBackColor; // remember background color
    addMouseListener(new GameMouseListener()); // add mouse listener to panel directly so mouse coordinates are correct
    // load card images
    try {
      Card.loadImgs();
    }
    catch (FileNotFoundException ex) {
      System.out.println(ex.getMessage());
      System.out.println("The program will exit now.");
      System.exit(1);
    }
    // indicate that no game is currently being played
    gameOver = true;
    errorMsg = "";
  }

  /** start a new German Whist game */
  public void newGame(ArrayList<String> playerNames, long seed, PlayerSocket networkSocket) {
    int i;
    rand = new Random(seed); // initialize random number generator to same seed as other clients
    socket = networkSocket;
    // throw exception if invalid number of players playing
    if (playerNames.size() < MinPlayers || playerNames.size() > MaxPlayers) {
      throw new IllegalArgumentException("Number of players must be between " + MinPlayers + " and " + MaxPlayers);
    }
    // set up talon
    talon = new Deck();
    talon.initStd52CardDeck();
    talon.shuffle(rand);
    // remove cards from talon if doesn't divide evenly into number of players
    while (talon.size() % playerNames.size() != 0) {
      talon.moveCardTo(new Deck(), talon.size() - 1);
    }
    // give error and exit if not enough cards available to deal to players
    if (playerNames.size() * NDealtCards > talon.size()) {
      System.out.println("Not enough cards in talon to deal to players.");
      System.out.println("The program will exit now.");
      System.exit(1);
    }
    else if (playerNames.size() * NDealtCards == talon.size()) {
      // last card dealt will be the last card in the talon, so determine trump suit now
      trump = talon.getCard(0).getSuit();
    }
    // set up players
    clientPlayer = -1;
    players = new ArrayList<Player>();
    for (i = 0; i < playerNames.size(); i++) {
      Player player = new Player(playerNames.get(i), i);
      player.dealFrom(talon, NDealtCards); // deal cards to player
      players.add(player); // add player to list
      if (socket.getPlayerName().equals(playerNames.get(i))) {
        clientPlayer = i;
      }
    }
    if (clientPlayer < 0) {
      throw new IllegalArgumentException("Client is not one of the players");
    }
    prevWinner = 0;
    Player.setActivePlayer(0);
    // remember trump suit (unless talon is empty, in which case trump suit was determined earlier)
    if (talon.size() > 0) {
      trump = talon.getTopCard().getSuit();
    }
    // begin a new trick
    gameOver = false;
    errorMsg = "";
    trick = new Deck();
    talon.setTopFaceUp(true);
    // repaint the panel
    repaint();
  }

  /** end game in its current state
      (this is not a valid state to display game in,
       so caller should hide game panel immediately after calling this) */
  public void stopGame() {
    GameOverMsg msgOut;
    gameOver = true;
    if (players != null) {
      for (int i = 0; i < players.size(); i++) {
        players.get(i).stopGame();
      }
    }
    finalWinnerTie = true; // if paint() is accidentally called, don't display a false winner
    // notify server that game ended before completion
    msgOut = new GameOverMsg();
    msgOut.complete = false;
    socket.write(msgOut);
  }

  /** check if game has ended, and if so declare the winner */
  private void evaluateGame() {
    int i;
    // check if game has ended
    for (i = 0; i < players.size(); i++) {
      if (!players.get(i).handIsEmpty()) {
        break;
      }
    }
    if (i == players.size()) {
      // game has ended, declare the winner
      finalWinner = 0;
      finalWinnerTie = false;
      for (i = 1; i < players.size(); i++) {
        if (players.get(i).getScore() > players.get(finalWinner).getScore()) {
          finalWinner = i;
          finalWinnerTie = false;
        }
        else if (players.get(i).getScore() == players.get(finalWinner).getScore()) {
          finalWinnerTie = true;
        }
      }
      gameOver = true;
    }
  }

  /** play card at the specified point for a trick */
  private void playTrickAt(Point clickPos) {
    int playCard = players.get(Player.getActivePlayer()).getHandCardAt(this, clickPos);
    if (clientPlayer == Player.getActivePlayer()) {
      playTrick(playCard);
    }
    else if (playCard >= 0) {
      errorMsg = "It's not your turn to play a card.";
      repaint();
    }
  }

  /** play specified card in next player's hand for a trick,
      returns whether card played was valid */
  public boolean playTrick(int playCard) {
    boolean ret = false;
    if (playCard >= 0) { // check that user clicked on a card
      if (Player.getActivePlayer() == prevWinner) {
        trick = new Deck(); // clear trick deck for a new trick
      }
      if (players.get(Player.getActivePlayer()).playTrick(playCard, trick)) {
        // valid card was played
        if (clientPlayer + 1 == Player.getActivePlayer()) {
          // play made by client's player, send message to server
          socket.write(new GamePlayMsg(playCard));
        }
        errorMsg = "";
        if (Player.getActivePlayer() >= players.size()) {
          Player.setActivePlayer(0);
        }
        if (Player.getActivePlayer() == prevWinner) {
          // all players played a card, evaluate the trick
          evaluateTrick();
        }
        if (gameOver) {
          // game ended, notify server
          // (this sends a duplicate message from every client, but server can handle that)
          GameOverMsg msgOut;
          msgOut = new GameOverMsg();
          msgOut.complete = true;
          msgOut.tie = finalWinnerTie;
          msgOut.winner = finalWinner;
          socket.write(msgOut);
        }
        ret = true;
      }
      else {
        // invalid card clicked, display message
        errorMsg = "You must play a card of the suit led, since you have such a card.";
      }
      repaint();
    }
    return ret;
  }

  /** decide the winner of current trick,
      then either begin a new trick or declare the game winner */
  private void evaluateTrick() {
    int i;
    // decide who won the trick
    int winner = 0;
    for (i = 1; i < players.size(); i++) {
      if ((trick.getCard(i).getSuit() == trick.getCard(winner).getSuit() && trick.getCard(i).getRank() > trick.getCard(winner).getRank())
          || (trick.getCard(i).getSuit() == trump && trick.getCard(winner).getSuit() != trump)) {
        winner = i;
      }
    }
    winner = (prevWinner + winner) % players.size();
    players.get(winner).winTrick();
    prevWinner = winner;
    errorMsg = players.get(winner).getName() + " wins the trick!"; // technically not an error message, but it's a good place to display it
    // begin a new trick
    if (talon.size() > 0) {
      players.get(winner).dealFrom(talon, 1);
      for (i = 0; i < players.size(); i++) {
        if (i != winner) {
          if (talon.size() == 0) {
            break;
          }
          players.get(i).dealFrom(talon, 1);
        }
      }
    }
    talon.setTopFaceUp(true);
    // handle whether game has ended
    evaluateGame();
  }

  /** draw all cards and text (triggered when the screen refreshes) */
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    int i, textLeft;
    // fill window with background color
    g2.setColor(backColor);
    g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));
    // draw talon
    talon.draw(this, g2, new Point(getWidth() - Padding - Card.getImgWidth() / 2, Padding + 50 + Card.getImgWidth() / 2), new Dimension());
    // draw trick deck
    trick.draw(this, g2, new Point(Padding + Card.getImgWidth() / 2, Padding + Card.getImgHeight() / 2), new Dimension(Card.getImgWidth() * players.size(), 0));
    // draw player hands
    //for (i = 0; i < players.size(); i++) {
      players.get(clientPlayer).drawHand(this, g2);
    //}
    // draw status text
    textLeft = Padding * 2 + Card.getImgWidth() * players.size();
    g2.setColor(Color.black);
    if (!gameOver) {
      g2.drawString(players.get(Player.getActivePlayer()).getName() + "'s turn"
                    + ((clientPlayer == Player.getActivePlayer()) ? " (YOUR MOVE)" : ""), textLeft, Padding + TextHeight);
    }
    g2.drawString(trump.toString() + " is the trump suit", textLeft, Padding + TextHeight * 2);
    for (i = 0; i < players.size(); i++) {
      g2.drawString(players.get(i).getName() + "'s score: " + players.get(i).getScore(), textLeft, Padding + TextHeight * (i + 3));
    }
    // draw game over text
    if (gameOver) {
      if (finalWinnerTie) {
        g2.drawString("It's a draw! Click Game > Game Lobby to start a new game.",
                      textLeft, Padding + TextHeight * (players.size() + 4));
      }
      else {
        g2.drawString(players.get(finalWinner).getName() + " wins the game! Click Game > Game Lobby to start a new game.",
                      textLeft, Padding + TextHeight * (players.size() + 4));
      }
    }
    // draw error text
    if (!errorMsg.isEmpty()) {
      g2.drawString(errorMsg, Padding, Padding + Card.getImgHeight() + TextHeight);
    }
  }

  /** getter for whether game has ended */
  public final boolean getGameOver() {
    return gameOver;
  }

  /** class to handle in-game mouse events */
  private class GameMouseListener extends MouseAdapter {
    /** handles mouse clicks to play a card */
    public void mouseClicked(MouseEvent e) {
      if (!gameOver) { // ignore mouse clicks if game has ended
        playTrickAt(e.getPoint()); // play a card for a trick
      }
    }
  }
}
