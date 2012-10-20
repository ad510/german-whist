import java.io.*;
import java.util.*;

/** networking message describing current game session
    (separate from GameSession class for a strange networking reason) */
public class GameSessionMsg implements Serializable {
  /** list of names of players that have joined the game
      (strangely if I write an ArrayList<String> then only the first element is received over the network,
       but writing a char[] works) */
  public ArrayList<char[]> players;
  /** whether the game has started
      (alternately, whether a client is joining as opposed to leaving the game) */
  public boolean playing;

  /** constructor that converts GameSession to GameSessionMsg */
  public GameSessionMsg(GameSession msg) {
    players = new ArrayList<char[]>();
    for (int i = 0; i < msg.players.size(); i++) {
      players.add(msg.players.get(i).toCharArray());
    }
    playing = msg.playing;
  }

  /** returns GameSession that is equivalent to this GameSessionMsg */
  public GameSession toGameSession() {
    GameSession ret = new GameSession();
    for (int i = 0; i < players.size(); i++) {
      ret.players.add(new String(players.get(i)));
    }
    ret.playing = playing;
    return ret;
  }
}
