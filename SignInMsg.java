import java.io.*;

/** networking message to sign in to (existing or new) player */
public class SignInMsg implements Serializable {
  /** name of player that is being signed in to */
  public String playerName;
  /** password of player account */
  public String password;
  /** whether creating new account (as opposed to signing in to existing one) */
  public boolean newPlayer;
}
