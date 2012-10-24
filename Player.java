//
// Player.java
// author: Andrew Downing
//

import java.util.*;
import java.awt.*;
import java.io.*;

/** represents a single player of the game German Whist and provides a wrapper around the player's hand deck */
public class Player implements Serializable {
  // per-game variables
  private static int activePlayer; /**< id of player who is currently playing a card */
  private int id; /**< determines playing order during a game */
  private Deck hand; /**< deck containing this player's cards */
  private int score; /**< number of tricks this player won */

  // persistent variables
  private String name; /**< name of this player */
  private int gamesWon; /**< number of games this player won (doesn't include ties) */
  private int gamesPlayed; /**< total number of games this player finished playing
                                (includes ties but doesn't include incomplete games) */

  /** constructor for German Whist player */
  public Player(String newName, int newGamesWon, int newGamesPlayed) {
    if (!Application.isValidName(newName)) {
      throw new IllegalArgumentException("Player name is not a valid name");
    }
    if (newGamesWon < 0 || newGamesPlayed < 0) {
      throw new IllegalArgumentException("Games won and played must both be nonnegative");
    }
    name = newName;
    gamesWon = newGamesWon;
    gamesPlayed = newGamesPlayed;
  }

  /** prepare player for a new German Whist game */
  public void newGame(int newId, Deck talon) {
    id = newId;
    hand = new Deck();
    dealFrom(talon, GamePanel.NDealtCards); // deal cards to player
    score = 0;
  }

  /** handle ending game in its current state */
  public void stopGame() {
    hand = new Deck();
  }

  /** deal nCards cards from the talon to this player's hand */
  public void dealFrom(Deck talon, int nCards) {
    for (int i = 0; i < nCards; i++) {
      talon.moveCardTo(hand, talon.size() - 1);
    }
    hand.sort();
  }

  /** play specified card for a trick,
      returns whether the card was played (must play same suit as 1st card in trick if possible) */
  public boolean playTrick(int cardIndex, Deck trick) {
    // throw exception if card index out of bounds
    if (cardIndex < 0 || cardIndex >= hand.size()) {
      throw new IndexOutOfBoundsException("Card index out of bounds");
    }
    // card may be invalid if is different suit from 1st card in trick
    if (trick.size() > 0 && hand.getCard(cardIndex).getSuit() != trick.getCard(0).getSuit()) {
      // if not same suit as leading card, card is invalid if hand has a card with the same suit
      // (I didn't implement this for HW 2 because I misunderstood what "follow suit" meant)
      for (int i = 0; i < hand.size(); i++) {
        if (hand.getCard(i).getSuit() == trick.getCard(0).getSuit()) {
          return false;
        }
      }
    }
    // card is valid, play it for trick
    hand.moveCardTo(trick, cardIndex);
    activePlayer++;
    return true;
  }

  /** draw the cards and a label of this player's hand */
  public void drawHand(Component c, Graphics2D g2) {
    Point handPos = getHandPos(c);
    hand.setAllFaceUp(id == activePlayer); // draw cards face up only if this player is active
    hand.draw(c, g2, handPos, getHandSpread(c));
    g2.setColor(Color.black);
    g2.drawString(name + "'s hand", handPos.x - Card.getImgWidth() / 2, handPos.y - Card.getImgHeight() / 2 - GamePanel.TextHeight / 2);
  }

  /** helper function to position the hand */
  private final Point getHandPos(Component c) {
    return new Point(GamePanel.Padding + Card.getImgWidth() / 2,
                     c.getHeight() - GamePanel.Padding - Card.getImgHeight() / 2 - id * (GamePanel.Padding + Card.getImgHeight()));
  }

  /** helper function to find how much the hand is spread out when drawn */
  private final Dimension getHandSpread(Component c) {
    return new Dimension(c.getWidth() - GamePanel.Padding * 2 - Card.getImgWidth(), 0);
  }

  /** called when this player won a trick */
  public void winTrick() {
    score++;
    activePlayer = id;
  }

  /** called when this player won a game */
  public void winGame() {
    gamesWon++;
    gamesPlayed++;
  }

  /** called when this player lost or the game was a tie */
  public void loseGame() {
    gamesPlayed++;
  }

  /** getter for score (i.e., number of tricks won) */
  public final int getScore() {
    return score;
  }

  /** getter for player name */
  public final String getName() {
    return name;
  }

  /** getter for games won */
  public final int getGamesWon() {
    return gamesWon;
  }

  /** getter for games played */
  public final int getGamesPlayed() {
    return gamesPlayed;
  }

  /** returns index of card in hand at specified position, or -1 if there is no card in hand at specified position */
  public final int getHandCardAt(Component c, Point clickPos) {
    return hand.getCardAt(clickPos, getHandPos(c), getHandSpread(c));
  }

  /** returns whether the player's hand contains 0 cards */
  public final boolean handIsEmpty() {
    return (hand.size() == 0);
  }

  /** setter for currently active player */
  public static void setActivePlayer(int newActivePlayer) {
    activePlayer = newActivePlayer;
  }

  /** getter for currently active player */
  public static final int getActivePlayer() {
    return activePlayer;
  }
}
