import java.io.*;
import java.util.*;

/** description of a current game session */
public class GameSession implements Serializable {
  /** list of names of players that have joined the game */
  public ArrayList<String> players;
  /** whether the game has started
      (alternately, whether a client is joining as opposed to leaving the game) */
  public boolean playing;

  /** constructor for empty game session */
  public GameSession() {
    players = new ArrayList<String>();
    playing = false;
  }
}
