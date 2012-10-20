import java.io.*;
import java.net.*;

/** encapsulates Socket-related classes for a single player */
public class PlayerSocket {
  private Socket socket; /**< client socket that is endpoint for network communication */
  private ObjectInputStream in; /**< reads messages related to this player over the network */
  private ObjectOutputStream out; /**< writes messages related to this player over the network */
  private String playerName; /**< name of player associated with this socket */

  /** constructor for player socket */
  public PlayerSocket(Socket newSocket) throws IOException {
    if (newSocket == null) {
      throw new NullPointerException("Socket cannot be null");
    }
    socket = newSocket;
    try {
      out = new ObjectOutputStream(socket.getOutputStream()); // always add before input stream, see http://stackoverflow.com/questions/8088557/getinputstream-blocks
      in = new ObjectInputStream(socket.getInputStream());
      socket.setSoTimeout(50); // this practically makes reads non-blocking
    }
    // pass exceptions onto caller
    catch (IOException ex) {
      throw ex;
    }
  }

  /** read an object from input stream, returning null if no new message */
  public final Object read() {
    try {
      return in.readObject();
    }
    // exceptions below are thrown when no new messages
    catch (SocketTimeoutException ex) {
      return null;
    }
    catch (OptionalDataException ex) {
      return null;
    }
    // exceptions below are thrown when client disconnected
    catch (EOFException ex) {
      return new CloseConnectionMsg();
    }
    catch (SocketException ex) {
      return new CloseConnectionMsg();
    }
    catch (StreamCorruptedException ex) {
      System.out.println("Input stream corrupted: " + ex.getMessage());
      return new CloseConnectionMsg();
    }
    // print stack trace and return null if unknown error
    // (if I don't know what causes the error then I can't write code to handle it)
    catch (Exception ex) {
      System.out.println("Network read error:");
      ex.printStackTrace();
      return null;
    }
  }

  /** write specified object to output stream */
  public final void write(Object obj) {
    try {
      out.writeObject(obj);
      out.flush();
    }
    // print error message if unknown error
    // (if I don't know what causes the error then I can't write code to handle it)
    catch (IOException ex) {
      System.out.println("Network write error: " + ex.toString());
    }
  }

  /** close connection */
  public void close() {
    try {
      out.writeObject(new CloseConnectionMsg());
      out.flush();
    }
    catch (Exception ex) {
      // ignore exceptions
    }
  }

  /** setter for player name
      (does not throw exception for invalid name
       because no player in player list would have such a name) */
  public void setPlayerName(String name) {
    playerName = name;
  }

  /** getter for player name */
  public final String getPlayerName() {
    return playerName;
  }
}
