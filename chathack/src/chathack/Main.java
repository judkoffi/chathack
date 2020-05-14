package chathack;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Main {
  public static void main(String[] args) throws IOException {

    var addr = new InetSocketAddress("localhost", 7777);
    var sc = SocketChannel.open(addr);


    var bb = ByteBuffer.allocate(1024);

    bb.put((byte) 2);
    var login = StandardCharsets.UTF_8.encode("peach");
    var msg = StandardCharsets.UTF_8.encode("hello");


    bb.putInt(login.limit());
    bb.put(login);


    bb.putInt(msg.limit());
    bb.put(msg);

    bb.flip();
    sc.write(bb);
  }
}
