package chathack.main;

import static java.lang.Integer.valueOf;
import java.io.IOException;
import chathack.server.ServerChatHack;

public class ServerMain {
  public static void main(String[] args) throws NumberFormatException, IOException {
    if (args.length != 3) {
      usage();
      return;
    }
    new ServerChatHack(valueOf(args[0]), args[1], valueOf(args[2])).launch();
  }

  private static void usage() {
    System.out.println("Usage : ServerChathack serverPort dbHost dbPort");
  }
}
