import java.io.*;
import java.util.*;

/** networking message listing all game sessions */
public class GameSessionListMsg implements Serializable {
  public ArrayList<GameSessionMsg> games; /**< list of all (available) game sessions */

  /** returns GameSession list that is equivalent to GameSessionMsg list contained in message */
  public ArrayList<GameSession> toGameSessionList() {
    ArrayList<GameSession> ret = new ArrayList<GameSession>();
    for (int i = 0; i < games.size(); i++) {
      ret.add(games.get(i).toGameSession());
    }
    return ret;
  }
}
