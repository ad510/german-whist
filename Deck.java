// Deck.java
// Copyright (c) 2012 Andrew Downing
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

/** represents a collection of cards, regardless of whether they are displayed together or spread out */
public class Deck implements Serializable {
  private final int CardThickness = 1; /**< how far apart (in pixels) to draw cards when the deck is squared up */

  private ArrayList<Card> cards; /**< ArrayList of cards in this deck (cards towards the top have higher indices) */

  /** constructor for card deck */
  public Deck() {
    cards = new ArrayList<Card>();
  }

  /** append a standard 52-card deck with all cards face down */
  public void initStd52CardDeck() {
    int rank;
    // iterate through all possible suits and ranks
    for (Card.Suit suit : Card.Suit.values()) {
      for (rank = Card.MinRank; rank <= Card.MaxRank; rank++) {
        addCard(new Card(rank, suit, false));
      }
    }
  }

  /** randomize the order of the cards without changing which cards are in the deck */
  public void shuffle(Random rand) {
    ArrayList<Card> shuffledCards = new ArrayList<Card>();
    while (!cards.isEmpty()) {
      shuffledCards.add(cards.remove(rand.nextInt(cards.size())));
    }
    cards = shuffledCards;
  }

  /** sort cards by suit and rank */
  public void sort() {
    Card tempCard;
    int i, j, swapCard;
    for (i = 0; i < cards.size() - 1; i++) {
      // find card with lowest suit/rank combination to swap with
      swapCard = i;
      for (j = i + 1; j < cards.size(); j++) {
        if (cards.get(j).getSuit().ordinal() < cards.get(swapCard).getSuit().ordinal()
            || (cards.get(j).getSuit().ordinal() == cards.get(swapCard).getSuit().ordinal()
                && cards.get(j).getRank() < cards.get(swapCard).getRank())) {
          swapCard = j;
        }
      }
      if (i != swapCard) {
        // swap cards
        tempCard = cards.get(i);
        cards.set(i, cards.get(swapCard));
        cards.set(swapCard, tempCard);
      }
    }
  }

  /** add specified card to the top of the deck */
  public void addCard(Card card) {
    cards.add(card);
  }

  /** move card of specified index from this deck to specified deck */
  public void moveCardTo(Deck deck, int cardIndex) {
    if (cardIndex < 0 || cardIndex >= cards.size()) {
      throw new IndexOutOfBoundsException("Card index out of bounds");
    }
    deck.addCard(cards.remove(cardIndex));
  }

  /** make all cards in the deck face up */
  public void setAllFaceUp(boolean newFaceUp) {
    for (int i = 0; i < cards.size(); i++) {
      cards.get(i).setFaceUp(newFaceUp);
    }
  }

  /** make the top card face up */
  public void setTopFaceUp(boolean newFaceUp) {
    if (!cards.isEmpty()) {
      cards.get(cards.size() - 1).setFaceUp(newFaceUp);
    }
  }

  /** draw deck at specified position with specified total spread
      (position and spread should be regenerated during every repaint in case window is resized) */
  public void draw(Component c, Graphics2D g2, Point pos, Dimension spread) {
    Point2D.Double spreadPerCard = getSpreadPerCard(spread);
    for (int i = 0; i < cards.size(); i++) {
      cards.get(i).draw(c, g2, new Point((int)(pos.x + i * spreadPerCard.x), (int)(pos.y + i * spreadPerCard.y)));
    }
  }

  /** returns index of card at specified position, or -1 if there is no card at specified position */
  public final int getCardAt(Point clickPos, Point pos, Dimension spread) {
    Point2D.Double spreadPerCard = getSpreadPerCard(spread);
    Point cardPos;
    // iterate through cards in reverse order to check cards at top first
    for (int i = cards.size() - 1; i >= 0; i--) {
      cardPos = new Point((int)(pos.x + i * spreadPerCard.x - Card.getImgWidth() / 2),
                          (int)(pos.y + i * spreadPerCard.y - Card.getImgHeight() / 2));
      if (new Rectangle(cardPos, new Dimension(Card.getImgWidth(), Card.getImgHeight())).contains(clickPos)) {
        return i;
      }
    }
    return -1;
  }

  /** returns difference in position between consecutive cards in deck
      (returns a Point2D.Double because there is no Dimension2D.Double) */
  private final Point2D.Double getSpreadPerCard(Dimension spread) {
    if ((spread.width == 0 && spread.height == 0) || cards.size() <= 1) {
      // deck is squared up, so give it a 3D appearance
      return new Point2D.Double(-CardThickness, -CardThickness);
    }
    else {
      // cards in deck are spread out, so calculate spread per card based on total deck spread
      return new Point2D.Double(Math.min((double)(spread.width) / (cards.size() - 1), Card.getImgWidth()),
                                Math.min((double)(spread.height) / (cards.size() - 1), Card.getImgHeight()));
    }
  }

  /** returns String containing rank and suit of all cards in deck */
  public final String getString() {
    StringBuilder ret = new StringBuilder();
    for (int i = 0; i < cards.size(); i++) {
      ret.append(cards.get(i).getString());
      ret.append(" ");
    }
    return ret.toString();
  }

  /** returns card at the specified position of the deck */
  public final Card getCard(int cardIndex) {
    if (cardIndex < 0 || cardIndex >= cards.size()) {
      throw new IndexOutOfBoundsException("Card index out of bounds");
    }
    return cards.get(cardIndex);
  }

  /** returns index of card in deck equal to specified card,
      or -1 if deck doesn't contain such a card */
  public final int getEqualCard(Card card) {
    for (int i = 0; i < cards.size(); i++) {
      if (card.equals(cards.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /** returns card at the top of the deck */
  public final Card getTopCard() {
    if (cards.isEmpty()) {
      throw new IndexOutOfBoundsException("Deck is empty");
    }
    return cards.get(cards.size() - 1);
  }

  /** returns number of cards in deck */
  public final int size() {
    return cards.size();
  }
}
