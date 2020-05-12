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
import chathack.common.reader.IReader;
import chathack.common.reader.MessageReader;
import chathack.common.trame.Message;
import chathack.writer.MessageWriter;

public class ServerChatHack {
  private static final Logger logger = Logger.getLogger(ServerChatHack.class.getName());
  private final ServerSocketChannel serverSocketChannel;
  private final Selector selector;
  // private final SocketChannel databaseChannel;
  private final MessageWriter msgWriter = new MessageWriter();

  public ServerChatHack(int port, String dbHost, int dbPort) throws IOException {
    serverSocketChannel = ServerSocketChannel.open();
    serverSocketChannel.bind(new InetSocketAddress(port));
    // databaseChannel = SocketChannel.open(new InetSocketAddress(dbHost, dbPort));
    selector = Selector.open();
  }

  public void launch() throws IOException {
    serverSocketChannel.configureBlocking(false);
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

    // databaseChannel.configureBlocking(false);
    // databaseChannel.register(selector, SelectionKey.OP_CONNECT);

    while (!Thread.interrupted()) {
      // System.out.println("Starting select");
      try {
        selector.select(this::treatKey);
      } catch (UncheckedIOException tunneled) {
        throw tunneled.getCause();
      }
      // System.out.println("Select finished");
    }
  }

  private void treatKey(SelectionKey key) {
    try {
      if (key.isValid() && key.isConnectable()) {
        doConnect(key);
      }
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }

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

  /*
   * Use to perform connection with database server
   */
  public void doConnect(SelectionKey key) throws IOException {
    // if (!databaseChannel.finishConnect())
    // return; // the selector gave a bad hint
    // var dbKey = key.interestOps(SelectionKey.OP_WRITE);
    // dbKey.attach(new Context(this, dbKey));
    // System.out.println("database connected");

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


  private static class Context {
    private final SelectionKey key;
    private final SocketChannel sc;
    private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
    private final Queue<Message> queue = new LinkedList<>();
    private final ServerChatHack server;
    private final MessageReader messageReader = new MessageReader();
    private boolean closed = false;
    private String login;

    public Context(ServerChatHack server, SelectionKey key) {
      this.key = key;
      this.sc = (SocketChannel) key.channel();
      this.server = server;
    }

    public void processIn() {
      for (;;) {
        IReader.ProcessStatus status = messageReader.process(bbin);
        switch (status) {
          case DONE:
            Message msg = messageReader.get();
            switch (msg.getOpcode()) {
              case ANONYMOUS_CLIENT_CONNECTION:
                login = msg.getLogin();
                break;
              case AUTHENTICATED_CLIENT_CONNECTION:
                break;
              case BROADCOAST_MESSAGE:
                break;
              case REQUEST_PRIVATE_CLIENT_CONNECTION:
                break;
              default:
                break;
            }

            messageReader.reset();
            break;
          case REFILL:
            return;
          case ERROR:
            silentlyClose();
            return;
        }
      }
    }


    public void queueMessage(Message msg) {
      queue.add(msg);
      processOut();
      updateInterestOps();
    }


    public void processOut() {
      while (!queue.isEmpty()) {
        Message message = queue.peek();
        System.out.println("msg: " + message);
        if (bbout.remaining() < message.getTotalSize())
          return;

        bbout.put(message.toBuffer());
        queue.remove();
      }
    }



    public void updateInterestOps() {
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


    public void doRead() throws IOException {
      var read = sc.read(bbin);
      if (read == -1)
        closed = true;
      processIn();
      updateInterestOps();
    }

    public void doWrite() throws IOException {
      bbout.flip();
      sc.write(bbout);
      bbout.compact();
      processOut();
      updateInterestOps();
    }
  }
}
