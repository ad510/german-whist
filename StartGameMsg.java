import java.io.*;
import java.util.*;

/** networking message indicating to start game with specified players and random number seed */
public class StartGameMsg implements Serializable {
  public GameSessionMsg players; /**< contains list of players in upcoming game */
  public long seed; /**< random number seed to use to generate game */

  /** constructor that initializes seed to a random number */
  public StartGameMsg() {
    seed = (new Random()).nextLong();
  }
}
