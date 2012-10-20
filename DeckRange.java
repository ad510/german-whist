// DeckRange.java
// Copyright (c) 2012 Andrew Downing
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

/** describes range of cards in a deck */
public class DeckRange {
  public int[] low; /**< lowest rank of cards in deck per suit */
  public int[] lowIndex; /**< index of card with lowest rank in deck per suit */
  public int[] high; /**< highest rank of cards in deck per suit */
  public int[] highIndex; /**< index of card with highest rank in deck per suit */

  /** constructor that calculates range of deck */
  public DeckRange(Deck deck) {
    int nSuit = Card.Suit.values().length;
    int suit, i;
    low = new int[nSuit];
    lowIndex = new int[nSuit];
    high = new int[nSuit];
    highIndex = new int[nSuit];
    for (i = 0; i < nSuit; i++) {
      low[i] = Card.MaxRank + 1;
      lowIndex[i] = -1;
      high[i] = 0;
      highIndex[i] = -1;
    }
    for (i = 0; i < deck.size(); i++) {
      suit = deck.getCard(i).getSuit().ordinal();
      if (deck.getCard(i).getRank() < low[suit]) {
        low[suit] = deck.getCard(i).getRank();
        lowIndex[suit] = i;
      }
      if (deck.getCard(i).getRank() > high[suit]) {
        high[suit] = deck.getCard(i).getRank();
        highIndex[suit] = i;
      }
    }
  }
}
