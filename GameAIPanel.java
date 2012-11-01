// GameAIPanel.java
// Copyright (c) 2012 Andrew Downing
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import java.util.*;
import java.awt.*;

/** German Whist game panel whose client's player is an AI player */
public class GameAIPanel extends GamePanel {
  private static final int DelayInterval = 1000; /**< sleep time in milliseconds before playing card */

  private int nSuit; /**< number of suits */
  private Deck hand; /**< reference to AI's hand deck */
  private Deck played; /**< contains list of cards that have been played */
  private Deck notPlayed; /**< contains list of cards that haven't been played (excluding own cards) */
  private DeckRange handRange; /**< range of rank of cards in our hand per suit */
  private DeckRange playedRange; /**< range of rank of cards played per suit */
  private DeckRange notPlayedRange; /**< range of rank of cards not played per suit */
  private ArrayList<Integer> handWeight; /**< eventually determines odds of playing of each card in hand */

  /** constructor to set up game and AI for the first time */
  public GameAIPanel(Color newBackColor) {
    super(newBackColor);
    nSuit = Card.Suit.values().length;
  }

  /** start a new German Whist game */
  public void newGame(ArrayList<String> playerNames, long seed, PlayerSocket networkSocket) {
    super.newGame(playerNames, seed, networkSocket);
    initAI();
    evaluateAI();
  }

  /** play specified card and also play AI's card if it is the AI's turn,
      returns whether card played was valid */
  public boolean playTrick(int playCard) {
    boolean ret;
    ret = super.playTrick(playCard);
    if (Player.getActivePlayer() == prevWinner) {
      trick = new Deck(); // clear trick deck for a new trick early so AI isn't confused
    }
    evaluateAI();
    return ret;
  }

  /** update AI stats, check if it's the AI's turn, and call playAITrick to play AI card if so */
  private void evaluateAI() {
    updateAIStats();
    if (!gameOver && clientPlayer == Player.getActivePlayer()) {
      // sleep before playing card so other clients get a chance to see previous card played
      try {
        Thread.sleep(DelayInterval);
      }
      catch (Exception ex) {
        // don't worry about exception thrown when sleeping
      }
      // it is AI's turn, play AI's card
      if (!playTrick(playAITrick())) {
        System.out.println("Warning: AI requested to play invalid card");
      }
    }
  }

  /** prepares AI for a new game */
  private void initAI() {
    played = new Deck();
    notPlayed = new Deck();
    notPlayed.initStd52CardDeck();
  }

  /** updates statistics AI stores without playing a card */
  private void updateAIStats() {
    int i, j;
    hand = getAIHand();
    // remove cards we own from not played deck
    for (i = 0; i < hand.size(); i++) {
      j = notPlayed.getEqualCard(hand.getCard(i));
      if (j >= 0) {
        notPlayed.moveCardTo(new Deck(), j);
      }
    }
    // move cards in trick deck from not played to played deck
    for (i = 0; i < trick.size(); i++) {
      j = notPlayed.getEqualCard(trick.getCard(i));
      if (j >= 0) {
        notPlayed.moveCardTo(played, j);
      }
    }
  }

  /** returns card that AI wants to play */
  private final int playAITrick() {
    int totalWeight; // total weight of all cards in hand
    int i, j;
    hand = getAIHand();
    // find worst and best cards unplayed, played, and in our hand
    handRange = new DeckRange(hand);
    playedRange = new DeckRange(played);
    notPlayedRange = new DeckRange(notPlayed);
    // reset weights
    handWeight = new ArrayList<Integer>();
    for (i = 0; i < hand.size(); i++) {
      if (isAIValidPlay(i)) {
        handWeight.add(1);
      }
      else {
        handWeight.add(0);
      }
    }
    // print status
    System.out.println();
    System.out.println("cards played:");
    System.out.println(played.getString());
    System.out.println("cards not played:");
    System.out.println(notPlayed.getString());
    for (Card.Suit currSuit : Card.Suit.values()) {
      System.out.println("hand " + currSuit.toString() + " rank range: "
                         + handRange.low[currSuit.ordinal()] + "-" + handRange.high[currSuit.ordinal()]);
      System.out.println("played " + currSuit.toString() + " rank range: "
                         + playedRange.low[currSuit.ordinal()] + "-" + playedRange.high[currSuit.ordinal()]);
      System.out.println("unplayed " + currSuit.toString() + " rank range: "
                         + notPlayedRange.low[currSuit.ordinal()] + "-" + notPlayedRange.high[currSuit.ordinal()]);
    }
    // decide whether to try to win or lose trick
    if (talon.size() == 0) {
      System.out.println("no cards in talon, so try to win trick");
      tryWinTrick();
    }
    else {
      j = talon.getTopCard().getSuit().ordinal();
      if (talon.getTopCard().getRank() > (Card.MaxRank - Card.MinRank) / 2 + Card.MinRank) {
        System.out.println("top talon card has good rank, so try to win trick");
        tryWinTrick();
      }
      else {
        System.out.println("top talon card has bad rank, so try to lose trick");
        tryLoseTrick();
      }
    }
    // decide card to play based on weights
    totalWeight = 0;
    System.out.println("hand weight distribution:");
    for (i = 0; i < hand.size(); i++) {
      totalWeight += handWeight.get(i);
      System.out.print(handWeight.get(i) + " ");
    }
    System.out.println();
    j = (int)Math.floor(Math.random() * (totalWeight + 1));
    for (i = 0; i < hand.size(); i++) {
      totalWeight -= handWeight.get(i);
      if (totalWeight <= j && handWeight.get(i) > 0) {
        return i;
      }
    }
    throw new RuntimeException("AI's random number didn't fall within weights; this shouldn't happen");
  }

  /** assign weights to try to win trick */
  public void tryWinTrick() {
    int thisRank, thisSuit;
    for (int i = 0; i < hand.size(); i++) {
      // make sure we follow suit if we're not playing first
      if (trick.size() == 0 || hand.getCard(i).getSuit() == trick.getCard(0).getSuit()) {
        thisRank = hand.getCard(i).getRank();
        thisSuit = hand.getCard(i).getSuit().ordinal();
        if (thisRank > notPlayedRange.high[thisSuit]) {
          handWeight.set(i, handWeight.get(i) + 500); // give very large weight if no other player has card ranked this high
        }
        if (thisRank > playedRange.high[thisSuit]) {
          handWeight.set(i, handWeight.get(i) + 50); // give large weight if other players haven't played a card ranked this high
        }
        handWeight.set(i, handWeight.get(i) + thisRank); // value larger ranks
      }
      else if (handWeight.get(i) > 0 && hand.getCard(i).getSuit() == trump) {
        // give large weight if we can't follow suit and this is a trump card
        handWeight.set(i, handWeight.get(i) + 50);
      }
    }
  }

  /** assign weights to try to lose trick */
  public void tryLoseTrick() {
    int thisRank, thisSuit;
    for (int i = 0; i < hand.size(); i++) {
      // make sure we follow suit if we're not playing first
      if (trick.size() == 0 || hand.getCard(i).getSuit() == trick.getCard(0).getSuit()) {
        thisRank = hand.getCard(i).getRank();
        thisSuit = hand.getCard(i).getSuit().ordinal();
        if (thisRank < notPlayedRange.low[thisSuit]) {
          handWeight.set(i, handWeight.get(i) + 500); // give very large weight if no other player has card ranked this low
        }
        if (thisRank < playedRange.low[thisSuit]) {
          handWeight.set(i, handWeight.get(i) + 50); // give large weight if other players haven't played a card ranked this low
        }
        handWeight.set(i, handWeight.get(i) + Card.MaxRank - thisRank); // value lower ranks
      }
    }
  }

  // commented out dumb AI below
  /*private void initAI() {
    // no initialization needed for dumb AI
  }

  private final int playAITrick() {
    int ret;
    do {
      ret = (int)Math.floor(Math.random() * getAIHand().size());
    } while (!isAIValidPlay(ret));
    return ret;
  }*/

  /** returns whether specified card is valid for AI to play for a trick */
  private final boolean isAIValidPlay(int cardIndex) {
    return players.get(clientPlayer).isValidPlay(cardIndex, trick);
  }

  /** returns AI player's hand deck */
  private final Deck getAIHand() {
    return players.get(clientPlayer).getHand();
  }
}
