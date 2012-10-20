import java.io.*;

/** networking message sent when game has ended */
public class GameOverMsg implements Serializable {
  boolean complete; /**< whether game was played to completion */
  boolean tie; /**< whether the game was a draw */
  int winner; /**< ID of game winner */
}
