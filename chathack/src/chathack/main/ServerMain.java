package chathack.main;

import java.io.IOException;
import chathack.server.ServerChatHack;

public class ServerMain {
  public static void main(String[] args) throws NumberFormatException, IOException {
    if (args.length != 1) {
      usage();
      return;
    }
    new ServerChatHack(Integer.parseInt(args[0])).launch();
  }

  private static void usage() {
    System.out.println("Usage : ServerChathack port");
  }
}
