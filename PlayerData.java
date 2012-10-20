import java.io.*;

/** information about each player that is saved to file between sessions */
public class PlayerData implements Serializable {
  private String name; /**< name of this player */
  private String password; /**< password to sign in to this player's account */
  private int gamesWon; /**< number of games this player won (doesn't include ties) */
  private int gamesPlayed; /**< total number of games this player finished playing
                                (includes ties but doesn't include incomplete games) */

  /** constructor for persistent player data */
  public PlayerData(String newName, String newPassword, int newGamesWon, int newGamesPlayed) {
    if (!isValidName(newName)) {
      throw new IllegalArgumentException("Player name is not a valid name");
    }
    if (newGamesWon < 0 || newGamesPlayed < 0) {
      throw new IllegalArgumentException("Games won and played must both be nonnegative");
    }
    name = newName;
    setPassword(newPassword);
    gamesWon = newGamesWon;
    gamesPlayed = newGamesPlayed;
  }

  /** called when this player won a game */
  public void winGame() {
    gamesWon++;
    gamesPlayed++;
  }

  /** called when this player lost or the game was a tie */
  public void loseGame() {
    gamesPlayed++;
  }

  /** setter for password */
  public void setPassword(String newPassword) {
    if (newPassword.isEmpty()) {
      throw new IllegalArgumentException("Password may not be empty");
    }
    password = newPassword;
  }

  /** returns whether specified credentials match with this player account */
  public final boolean signInMatch(String checkName, String checkPassword) {
    return name.equals(checkName) && password.equals(checkPassword);
  }

  /** getter for player name */
  public final String getName() {
    return name;
  }

  /** getter for games won */
  public final int getGamesWon() {
    return gamesWon;
  }

  /** getter for games played */
  public final int getGamesPlayed() {
    return gamesPlayed;
  }

  /** returns whether specified player name is valid
      (i.e. is not empty and is composed only of letters, numbers, or spaces) */
  public static boolean isValidName(String name) {
    if (name.isEmpty()) {
      return false;
    }
    for (char ch : name.toCharArray()) {
      if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch)) {
        return false;
      }
    }
    return true;
  }
}
