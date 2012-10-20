import java.io.*;
import java.util.*;

/** networking message to update leaderboard */
public class PlayerStatsMsg implements Serializable {
  /** list containing all player information stored on server (with passwords removed) */
  public ArrayList<PlayerData> players;
}
