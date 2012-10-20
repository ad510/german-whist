import java.io.*;

/** networking message to play a card */
public class GamePlayMsg implements Serializable {
  int card; /**< index of card in hand that was played */

  /** constructor for game play message */
  public GamePlayMsg(int cardPlayed) {
    card = cardPlayed;
  }
}
