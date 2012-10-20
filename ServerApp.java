import java.io.*;
import java.net.*;
import java.util.*;

/** class implementing a non-GUI server application to coordinate game client windows over a network */
public class ServerApp {
  public static int Port = 44247; /**< networking port that server listens on */
  public static int UpdateInterval = 200; /**< rate to poll for new messages, in milliseconds */
  private static final String SettingsPath = "save/players.dat"; /**< file path of player data file */

  /** server socket used to set up connections with clients */
  private ServerSocket serverSocket;
  /** ArrayList of clients connected to this server */
  private ArrayList<PlayerSocket> clients;
  /** ArrayList of all persistent player information */
  private ArrayList<PlayerData> players;
  /** ArrayList describing current game sessions */
  private ArrayList<GameSession> games;

  /** constructor for server application class */
  public ServerApp() throws IOException {
    int i;
    // initialize networking
    try {
      serverSocket = new ServerSocket(Port);
      serverSocket.setSoTimeout(1); // this practically makes accepting clients non-blocking
    }
    catch (IOException ex) {
      throw ex;
    }
    // instantiate lists
    clients = new ArrayList<PlayerSocket>();
    games = new ArrayList<GameSession>();
    // load player data from file
    loadSettings();
    // list players
    System.out.println("Server is ready; press ctrl+C to exit");
    System.out.println("Players on this server:");
    for (i = 0; i < players.size(); i++) {
      System.out.println(players.get(i).getName());
    }
    System.out.println();
  }

  public static void main(String[] args) {
    ServerApp app;
    try {
      app = new ServerApp();
    }
    catch (Exception ex) {
      System.out.println("Error initializing server:");
      ex.printStackTrace();
      return;
    }
    app.loop();
  }

  /** check for and handle new messages from clients at fixed time intervals */
  public void loop() {
    int i;
    try {
      while (true) { // loop exits when user presses ctrl+C
        try {
          // check for new clients
          try {
            Socket socket;
            do {
              socket = serverSocket.accept();
              clients.add(new PlayerSocket(socket));
              System.out.println(clientString(clients.size() - 1) + " has joined");
            } while (socket != null);
          }
          catch (SocketTimeoutException ex) {
            // ignore timeout
          }
          // check for new messages from clients
          for (i = 0; i < clients.size(); i++) {
            Object msgObj = null;
            do {
              msgObj = clients.get(i).read();
              if (msgObj != null) {
                // received a message, handle it
                //System.out.println("Received message from " + clientString(i) + ": " + msgObj);
                if (msgObj instanceof CloseConnectionMsg) {
                  // close connection with client
                  // (but don't call clients.get(i).close() because client might still receive the message and get confused)
                  leaveGame(i);
                  System.out.println(clientString(i) + " has left");
                  clients.remove(i);
                  msgObj = null;
                  i--;
                }
                else if (msgObj instanceof SignInMsg) {
                  // sign in or create new player
                  if (signIn(i, (SignInMsg)msgObj)) {
                    broadcastGames();
                  }
                  else {
                    System.out.println(clientString(i) + " unsuccessfully attempted to sign in");
                  }
                }
                else if (msgObj instanceof PlayerStatsMsg) {
                  // send updated leaderboard
                  sendLeaderboard(i);
                  System.out.println("Sent updated leaderboard to " + clientString(i));
                }
                else if (msgObj instanceof GameSessionMsg) {
                  // join or leave game (before it starts)
                  joinGame(i, ((GameSessionMsg)msgObj).toGameSession());
                }
                else if (msgObj instanceof StartGameMsg) {
                  // start game that this client has joined
                  if (startGame(i, (StartGameMsg)msgObj)) {
                    System.out.println(clientString(i) + " started a game");
                  }
                  else {
                    System.out.println(clientString(i) + " attempted to start a game at an invalid time");
                  }
                }
                else if (msgObj instanceof GamePlayMsg) {
                  // broadcast play to clients in a game
                  // (note that the client program needs to know the many of the game rules anyway,
                  //  such as whether a given card is a valid one to play and whether it's a valid time to play it,
                  //  so there's no point duplicating the game logic on the server)
                  broadcastPlay(i, (GamePlayMsg)msgObj);
                  System.out.println(clientString(i) + " played a card");
                }
                else if (msgObj instanceof GameOverMsg) {
                  // game ended, update player stats
                  if (endGame(i, (GameOverMsg)msgObj)) {
                    System.out.println("Game involving " + clientString(i) + " has ended");
                  }
                  else {
                    System.out.println("Game involving " + clientString(i) + " has ended (duplicate message)");
                  }
                }
                else if (msgObj instanceof StringMsg) {
                  StringMsg msg = (StringMsg)msgObj;
                  if (msg.type == StringMsg.MsgType.ChangePassword) {
                    // change password
                    if (changePassword(i, msg.message)) {
                      System.out.println(clientString(i) + " changed account password");
                    }
                    else {
                      System.out.println(clientString(i) + " unsuccessfully tried to change account password");
                    }
                  }
                  else if (msg.type == StringMsg.MsgType.DeleteAccount) {
                    // delete player account
                    if (deleteAccount(i, msg.message)) {
                      System.out.println(clientString(i) + " deleted player account");
                    }
                    else {
                      System.out.println(clientString(i) + " unsuccessfully tried to delete player account");
                    }
                  }
                  else {
                    System.out.println("Warning: received StringMsg of unknown type " + msg.type.toString() + " from " + clientString(i));
                  }
                }
                else {
                  System.out.println("Warning: received unknown message from " + clientString(i) + ": " + msgObj);
                }
              }
            } while (msgObj != null);
          }
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
        Thread.sleep(UpdateInterval);
      }
    }
    catch (InterruptedException ex) {
      System.out.println("Closing server...");
      Thread.currentThread().interrupt();
    }
  }

  /** sign in or create new player (returns whether sign in was successful) */
  private boolean signIn(int clientIndex, SignInMsg msg) {
    PlayerSocket client = clients.get(clientIndex);
    int i;
    if (msg.newPlayer) {
      // create new player
      if (!PlayerData.isValidName(msg.playerName)) {
        // player name is invalid, reject new player request
        client.write(new StringMsg(StringMsg.MsgType.SignInError, "Invalid player name"));
        return false;
      }
      for (i = 0; i < players.size(); i++) {
        if (players.get(i).getName().equals(msg.playerName)) {
          // another player has this name, reject new player request
          client.write(new StringMsg(StringMsg.MsgType.SignInError, "There is already a player named \"" + msg.playerName + "\""));
          return false;
        }
      }
      if (msg.password.isEmpty()) {
        // password is empty, reject new player request
        client.write(new StringMsg(StringMsg.MsgType.SignInError, "Password may not be empty"));
        return false;
      }
      // input is acceptable, create new player and save settings
      players.add(new PlayerData(msg.playerName, msg.password, 0, 0));
      saveSettings();
      client.setPlayerName(msg.playerName);
      client.write(new StringMsg(StringMsg.MsgType.SignInSuccess, msg.playerName));
      System.out.println(clientString(clientIndex) + " signed in to new player \"" + msg.playerName + "\"");
      return true;
    }
    else {
      // sign in to existing player
      for (i = 0; i < clients.size(); i++) {
        if (clients.get(i).getPlayerName() != null && clients.get(i).getPlayerName().equals(msg.playerName)) {
          // another user is logged on to this player, reject sign in request
          client.write(new StringMsg(StringMsg.MsgType.SignInError, "Another user is signed in to this player"));
          return false;
        }
      }
      for (i = 0; i < players.size(); i++) {
        if (players.get(i).signInMatch(msg.playerName, msg.password)) {
          // found matching credentials, complete successful sign in
          client.setPlayerName(msg.playerName);
          client.write(new StringMsg(StringMsg.MsgType.SignInSuccess, msg.playerName));
          System.out.println(clientString(clientIndex) + " signed in to existing player \"" + msg.playerName + "\"");
          return true;
        }
      }
      // didn't find any matching credentials, reject sign in request
      client.write(new StringMsg(StringMsg.MsgType.SignInError, "The player name or password you entered is incorrect"));
      return false;
    }
  }

  /** send updated leaderboard to specified client */
  private void sendLeaderboard(int clientIndex) {
    PlayerStatsMsg msgOut = new PlayerStatsMsg();
    msgOut.players = new ArrayList<PlayerData>();
    for (int i = 0; i < players.size(); i++) {
      // add players to list with passwords removed
      msgOut.players.add(new PlayerData(players.get(i).getName(), "\n", players.get(i).getGamesWon(), players.get(i).getGamesPlayed()));
    }
    clients.get(clientIndex).write(msgOut);
  }

  /** broadcast available game sessions to all non-playing clients */
  private void broadcastGames() {
    PlayerData player;
    GameSession game;
    int i;
    // prepare message
    GameSessionListMsg msg = new GameSessionListMsg();
    msg.games = new ArrayList<GameSessionMsg>();
    for (i = 0; i < games.size(); i++) {
      if (!games.get(i).playing && games.get(i).players.size() < GamePanel.MaxPlayers) {
        msg.games.add(new GameSessionMsg(games.get(i)));
      }
    }
    //System.out.println("msg.games.size " + msg.games.size());
    // broadcast message
    for (i = 0; i < clients.size(); i++) {
      if (clients.get(i).getPlayerName() != null) {
        game = playerGame(clients.get(i).getPlayerName());
        if (game == null) {
          // player hasn't joined game, send list of all games
          clients.get(i).write(msg);
        }
        else if (!game.playing) {
          // player has joined game, send that game only
          clients.get(i).write(new GameSessionMsg(game));
          //System.out.println("game.players.size " + game.players.size());
        }
      }
    }
  }

  /** let specified client join (or leave) specified game */
  private void joinGame(int clientIndex, GameSession msg) {
    PlayerSocket client = clients.get(clientIndex);
    GameSession game;
    if (client.getPlayerName() != null) {
      // remove this player from current game (if any)
      game = playerGame(client.getPlayerName());
      if (game != null && !game.playing) {
        game.players.remove(client.getPlayerName());
        if (game.players.isEmpty()) {
          games.remove(game);
        }
        System.out.println(clientString(clientIndex) + " left a game");
      }
      // if leaving game then done, otherwise add to specified game
      if (msg.playing) {
        if (msg.players == null || msg.players.isEmpty()) {
          // create new game and add player to it
          game = new GameSession();
          game.players.add(client.getPlayerName());
          games.add(game);
          System.out.println(clientString(clientIndex) + " hosted a new game");
        }
        else {
          // try to join existing game
          for (int i = 0; i < msg.players.size(); i++) {
            game = playerGame(msg.players.get(i));
            if (game != null && !game.playing && game.players.size() < GamePanel.MaxPlayers) {
              game.players.add(client.getPlayerName());
              System.out.println(clientString(clientIndex) + " joined game containing player \"" + msg.players.get(i) + "\"");
              break;
            }
          }
        }
      }
      broadcastGames();
    }
  }

  /** start game that specified client has joined, returns whether succeeded */
  private boolean startGame(int clientIndex, StartGameMsg msg) {
    StartGameMsg msgOut = new StartGameMsg();
    GameSession game = playerGame(clients.get(clientIndex).getPlayerName());
    if (game == null || game.playing) {
      return false;
    }
    game.playing = true;
    // broadcast start game message to all players in this game
    msgOut.seed = msg.seed; // setting random number seed ensures clients generate the same game
    msgOut.players = new GameSessionMsg(game);
    for (int i = 0; i < game.players.size(); i++) {
      clientNamed(game.players.get(i)).write(msgOut);
    }
    // broadcast updated available games list to everyone else
    broadcastGames();
    return true;
  }

  /** broadcast play to all clients in game, except the client who played */
  private void broadcastPlay(int clientIndex, GamePlayMsg msg) {
    GameSession game = playerGame(clients.get(clientIndex).getPlayerName());
    PlayerSocket client;
    if (game != null && game.playing) {
      for (int i = 0; i < game.players.size(); i++) {
        if (!clients.get(clientIndex).getPlayerName().equals(game.players.get(i))) {
          clientNamed(game.players.get(i)).write(msg);
        }
      }
    }
  }

  /** handle a game ending, returns whether succeeded */
  private boolean endGame(int clientIndex, GameOverMsg msg) {
    GameSession game = playerGame(clients.get(clientIndex).getPlayerName());
    int i;
    if (game == null || !game.playing) {
      return false;
    }
    if (msg.complete) {
      // game played to completion, so update player stats
      for (i = 0; i < game.players.size(); i++) {
        if (msg.tie || i != msg.winner) {
          playerNamed(game.players.get(i)).loseGame();
        }
        else {
          playerNamed(game.players.get(i)).winGame();
        }
      }
      saveSettings();
    }
    games.remove(game); // remove game from list
    broadcastGames(); // broadcast available games to join
    return true;
  }

  /** change password of specified client, returns whether succeeded */
  private boolean changePassword(int clientIndex, String newPassword) {
    PlayerSocket client = clients.get(clientIndex);
    PlayerData player = playerNamed(client.getPlayerName());
    if (player == null) {
      client.write(new StringMsg(StringMsg.MsgType.ChangePassword, "You are not signed in"));
      return false;
    }
    if (newPassword.isEmpty()) {
      client.write(new StringMsg(StringMsg.MsgType.ChangePassword, "Password may not be blank"));
      return false;
    }
    player.setPassword(newPassword);
    saveSettings();
    client.write(new StringMsg(StringMsg.MsgType.ChangePassword, "")); // send blank message to indicate success
    return true;
  }

  /** confirm password then delete player account of specified client, returns whether succeeded */
  private boolean deleteAccount(int clientIndex, String password) {
    PlayerSocket client = clients.get(clientIndex);
    PlayerData player = playerNamed(client.getPlayerName());
    if (player == null) {
      client.write(new StringMsg(StringMsg.MsgType.DeleteAccount, "You are not signed in"));
      return false;
    }
    if (password.isEmpty() || !player.signInMatch(player.getName(), password)) {
      client.write(new StringMsg(StringMsg.MsgType.DeleteAccount, "Incorrect password"));
      return false;
    }
    leaveGame(clientIndex); // remove client from game sessions
    players.remove(player);
    saveSettings();
    client.write(new StringMsg(StringMsg.MsgType.DeleteAccount, "")); // send blank message to indicate success
    return true;
  }

  /** remove client from current game, and stop game if it's already started */
  private void leaveGame(int clientIndex) {
    GameSession game = playerGame(clients.get(clientIndex).getPlayerName());
    if (game != null) {
      if (game.playing) {
        GameOverMsg msg = new GameOverMsg();
        msg.complete = false;
        endGame(clientIndex, msg);
      }
      else {
        joinGame(clientIndex, new GameSession());
      }
    }
  }

  /** returns reference to game that specified player has joined,
      or null if specified player didn't join a game */
  private final GameSession playerGame(String name) {
    int i, j;
    for (i = 0; i < games.size(); i++) {
      for (j = 0; j < games.get(i).players.size(); j++) {
        if (name.equals(games.get(i).players.get(j))) {
          return games.get(i);
        }
      }
    }
    return null;
  }

  /** returns player with specified name,
      or null if there is no such player */
  private final PlayerData playerNamed(String name) {
    if (name == null) {
      return null;
    }
    for (int i = 0; i < players.size(); i++) {
      if (name.equals(players.get(i).getName())) {
        return players.get(i);
      }
    }
    return null;
  }

  /** returns client with specified player name,
      or null if there is no such client */
  private final PlayerSocket clientNamed(String name) {
    if (name == null) {
      return null;
    }
    for (int i = 0; i < clients.size(); i++) {
      if (name.equals(clients.get(i).getPlayerName())) {
        return clients.get(i);
      }
    }
    return null;
  }

  /** returns string describing client of specified index */
  private final String clientString(int clientIndex) {
    return "Client " + clientIndex 
           + ((clients.get(clientIndex).getPlayerName() != null) ? (" (" + clients.get(clientIndex).getPlayerName() + ")") : "");
  }

  /** load player data from file */
  private void loadSettings() {
    try {
      ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(SettingsPath));
      Object inObj = inStream.readObject();
      if (!(inObj instanceof PlayerStatsMsg)) {
        throw new Exception("Invalid object in settings file");
      }
      players = ((PlayerStatsMsg)inObj).players;
      inStream.close();
    }
    catch (FileNotFoundException ex) {
      players = new ArrayList<PlayerData>();
    }
    catch (Exception ex) {
      System.out.println("Error loading player data from file. Using empty player list.");
      players = new ArrayList<PlayerData>();
    }
  }

  /** save player data to file */
  private void saveSettings() {
    try {
      ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(SettingsPath));
      PlayerStatsMsg outObj = new PlayerStatsMsg();
      outObj.players = players;
      outStream.writeObject(outObj);
      outStream.close();
    }
    catch (Exception ex) {
      System.out.println("Error saving player data to file.");
      System.out.println("Make sure the \"save\" folder exists.");
    }
  }
}
