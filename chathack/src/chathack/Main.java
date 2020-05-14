package chathack;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Main {

  private static void sendBroadcastMsg(SocketChannel sc, String message, String login)
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


  public static void main(String[] args) throws IOException {
    var addr = new InetSocketAddress("localhost", 7777);
    var sc = SocketChannel.open(addr);

    // sendAnonymousConnection(sc, "oeach");
    sendBroadcastMsg(sc, "hello", "peach");
  }
}
