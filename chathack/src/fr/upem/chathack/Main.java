package fr.upem.chathack;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

  private static void sendBroadcastMsg(SocketChannel sc, String login, String message)
      throws IOException {
    var bb = ByteBuffer.allocate(1024);
    bb.put((byte) 2);
    var loginbb = StandardCharsets.UTF_8.encode(login);
    var msgbb = StandardCharsets.UTF_8.encode(message);
    bb.putInt(loginbb.limit());
    bb.put(loginbb);
    bb.putInt(msgbb.limit());
    bb.put(msgbb);
    bb.flip();
    sc.write(bb);

    System.out.println("readdd");
    bb.clear();
    sc.read(bb);

    bb.flip();
    var size = bb.getInt();
    System.out.println("login size: " + size);
  }

  private static void sendAnonymousConnection(SocketChannel sc, String login) throws IOException {
    var bb = ByteBuffer.allocate(1024);
    bb.put((byte) 0);
    var loginbb = StandardCharsets.UTF_8.encode(login);
    bb.putInt(loginbb.limit());
    bb.put(loginbb);
    bb.flip();
    System.out.println(bb);
    sc.write(bb);
  }

  private static void sendAuthenticatedConnection(SocketChannel sc, String login, String pass)
      throws IOException {
    var bb = ByteBuffer.allocate(4096);
    var loginbb = StandardCharsets.UTF_8.encode(login);
    var passbb = StandardCharsets.UTF_8.encode(pass);
    bb.put((byte) 1);
    bb.putInt(loginbb.limit());
    bb.put(loginbb);
    bb.putInt(passbb.limit());
    bb.put(passbb);
    bb.flip();
    sc.write(bb);

    System.out.println("readdd");
    bb.clear();

    sc.read(bb);
    System.out.println(bb);
  }


  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage Main login");
      return;
    }

    var addr = new InetSocketAddress("localhost", 7777);
    var sc = SocketChannel.open(addr);

    var login = args[0];

    // sendAuthenticatedConnection(sc, login, "test");
    // sendAnonymousConnection(sc, login);
    sendBroadcastMsg(sc, login, "hello");
  }
}
