import java.io.*;

/** networking message containing a string */
public class StringMsg implements Serializable {
  /** categories that can be associated with a StringMsg */
  public enum MsgType {
    SignInSuccess, SignInError, ChangePassword, DeleteAccount
  }

  public MsgType type; /**< type of string message */
  public String message; /**< content of string message */

  /** constructor for string message */
  public StringMsg(MsgType newType, String newMessage) {
    type = newType;
    message = newMessage;
  }
}
