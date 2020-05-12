package chathack.server;

import static chathack.common.Helper.BUFFER_SIZE;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerChatHack {

  private static class Context {
    private final SelectionKey key;
    private final SocketChannel sc;
    private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
    private final Queue<String> queue = new LinkedList<>();
    private final ServerChatHack server;
    private boolean closed = false;

    private Context(ServerChatHack server, SelectionKey key) {
      this.key = key;
      this.sc = (SocketChannel) key.channel();
      this.server = server;
    }

    private void processIn() {
      // for (;;) {
      // Reader.ProcessStatus status = messageReader.process(bbin);
      // switch (status) {
      // case DONE:
      // break;
      // case REFILL:
      // return;
      // case ERROR:
      // silentlyClose();
      // return;
      // }
      // }
    }


    private void queueMessage(String msg) {

    }


    private void processOut() {

    }



    private void updateInterestOps() {
      if (!key.isValid())
        return;

      int interestOps = 0;

      if (!closed && bbin.hasRemaining()) {
        interestOps |= SelectionKey.OP_READ;
      }

      if (bbout.position() != 0 || !queue.isEmpty()) {
        interestOps |= SelectionKey.OP_WRITE;
      }

      if (interestOps == 0) {
        silentlyClose();
        return;
      }

      key.interestOps(interestOps);
    }

    private void silentlyClose() {
      try {
        sc.close();
      } catch (IOException e) {
        // ignore exception
      }
    }


    private void doRead() throws IOException {
      var read = sc.read(bbin);
      if (read == -1)
        closed = true;
      processIn();
      updateInterestOps();
    }

    private void doWrite() throws IOException {
      bbout.flip();
      sc.write(bbout);
      bbout.compact();
      processOut();
      updateInterestOps();
    }

  }

  private static final Logger logger = Logger.getLogger(ServerChatHack.class.getName());
  private final ServerSocketChannel serverSocketChannel;
  private final Selector selector;

  public ServerChatHack(int port) throws IOException {
    serverSocketChannel = ServerSocketChannel.open();
    serverSocketChannel.bind(new InetSocketAddress(port));
    selector = Selector.open();
  }

  public void launch() throws IOException {
    serverSocketChannel.configureBlocking(false);
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    while (!Thread.interrupted()) {
      System.out.println("Starting select");
      try {
        selector.select(this::treatKey);
      } catch (UncheckedIOException tunneled) {
        throw tunneled.getCause();
      }
      System.out.println("Select finished");
    }
  }

  private void treatKey(SelectionKey key) {
    try {
      if (key.isValid() && key.isAcceptable()) {
        doAccept(key);
      }
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
    try {
      if (key.isValid() && key.isWritable()) {
        ((Context) key.attachment()).doWrite();
      }
      if (key.isValid() && key.isReadable()) {
        ((Context) key.attachment()).doRead();
      }
    } catch (IOException e) {
      logger.log(Level.INFO, "Connection closed with client due to IOException", e);
      silentlyClose(key);
    }
  }

  private void doAccept(SelectionKey key) throws IOException {
    SocketChannel sc = serverSocketChannel.accept();
    if (sc == null)
      return; // the selector gave a bad hint
    sc.configureBlocking(false);
    SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    clientKey.attach(new Context(this, clientKey));
  }

  private void silentlyClose(SelectionKey key) {
    Channel sc = (Channel) key.channel();
    try {
      sc.close();
    } catch (IOException e) {
      // ignore exception
    }
  }

  private void broadcast() {

  }
}
