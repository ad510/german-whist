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
