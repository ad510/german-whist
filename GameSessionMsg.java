// GameSessionMsg.java
// Copyright (c) 2012 Andrew Downing
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

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
