package chathack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import chathack.context.DatabaseContext;
import chathack.context.ServerContext;

public class ServerChatHack {
  private static final Logger logger = Logger.getLogger(ServerChatHack.class.getName());
  private final Selector selector;
  private final ServerSocketChannel serverSocketChannel;
  private final SocketChannel dbChannel;
  private final HashMap<String, SelectionKey> map = new HashMap<>();
  private DatabaseContext databaseContext;
  private final InetSocketAddress databaseAddress;

  public ServerChatHack(int port, String dbHostname, int dbPort) throws IOException {
    serverSocketChannel = ServerSocketChannel.open();
    serverSocketChannel.bind(new InetSocketAddress(port));
    dbChannel = SocketChannel.open();
    databaseAddress = new InetSocketAddress(dbHostname, dbPort);
    selector = Selector.open();
  }


  public boolean registerClient(String login, SelectionKey clientKey) {
    System.out.println("login: " + login);
    if (!isAvailableLogin(login))
      return false;

    map.put(login, clientKey);
    return true;
  }

  private boolean isAvailableLogin(String login) {
    return !map.containsKey(login);
  }

  public void broadcast(ByteBuffer bb) {
    selector//
      .keys()
      .stream()
      .filter(k -> k.isValid())
      .filter(k -> k.attachment() != null)
      .filter(k -> !k.equals(databaseContext.getKey()))
      .forEach(k ->
      {
        // System.out.println(bb);
        var ctx = ((ServerContext) k.attachment());
        ctx.queueMessage(bb.duplicate());
      });
  }

  public void launch() throws IOException {
    serverSocketChannel.configureBlocking(false);
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    dbConnection();

    while (!Thread.interrupted()) {
      System.out.println("Starting select");
      printKeys();
      try {
        selector.select(this::treatKey);
      } catch (UncheckedIOException tunneled) {
        throw tunneled.getCause();
      }
      System.out.println("Select finished");
    }
  }

  private void dbConnection() throws IOException {
    dbChannel.configureBlocking(false);
    var dbKey = dbChannel.register(selector, SelectionKey.OP_CONNECT);
    databaseContext = new DatabaseContext(dbKey);
    dbKey.attach(databaseContext);
    dbChannel.connect(databaseAddress);
  }

  private void treatKey(SelectionKey key) {
    printSelectedKey(key);
    try {
      if (key.isValid() && key.isAcceptable()) {
        doAccept(key);
      }

      if (key.isValid() && key.isConnectable()) {
        databaseContext.doConnect();
      }
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }

    try {
      var attach = key.attachment();

      if (key.isValid() && key.isWritable()) {
        if (databaseContext == attach) {
          databaseContext.doRead();
        } else {
          ((ServerContext) key.attachment()).doWrite();
        }
      }

      if (key.isValid() && key.isReadable()) {
        if (databaseContext == attach) {
          databaseContext.doRead();
        } else {
          ((ServerContext) key.attachment()).doRead();
        }
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
    clientKey.attach(new ServerContext(clientKey, this));
  }

  private void silentlyClose(SelectionKey key) {
    Channel sc = (Channel) key.channel();
    try {
      sc.close();
    } catch (IOException e) {
      // ignore exception
    }
  }

  public static void main(String[] args) throws NumberFormatException, IOException {
    if (args.length != 3) {
      usage();
      return;
    }
    new ServerChatHack(Integer.valueOf(args[0]), args[1], Integer.valueOf(args[2])).launch();
  }

  private static void usage() {
    System.out.println("Usage : ServerChathack serverPort dbHost dbPort");
  }

  /***
   * Theses methods are here to help understanding the behavior of the selector
   ***/

  private String interestOpsToString(SelectionKey key) {
    if (!key.isValid()) {
      return "CANCELLED";
    }
    int interestOps = key.interestOps();
    ArrayList<String> list = new ArrayList<>();
    if ((interestOps & SelectionKey.OP_ACCEPT) != 0)
      list.add("OP_ACCEPT");
    if ((interestOps & SelectionKey.OP_READ) != 0)
      list.add("OP_READ");
    if ((interestOps & SelectionKey.OP_WRITE) != 0)
      list.add("OP_WRITE");
    return String.join("|", list);
  }

  public void printKeys() {
    Set<SelectionKey> selectionKeySet = selector.keys();
    if (selectionKeySet.isEmpty()) {
      System.out.println("The selector contains no key : this should not happen!");
      return;
    }
    System.out.println("The selector contains:");
    for (SelectionKey key : selectionKeySet) {
      SelectableChannel channel = key.channel();
      if (channel instanceof ServerSocketChannel) {
        System.out.println("\tKey for ServerSocketChannel : " + interestOpsToString(key));
      } else {
        SocketChannel sc = (SocketChannel) channel;
        System.out
          .println(
              "\tKey for Client " + remoteAddressToString(sc) + " : " + interestOpsToString(key));
      }
    }
  }

  private String remoteAddressToString(SocketChannel sc) {
    try {
      return sc.getRemoteAddress().toString();
    } catch (IOException e) {
      return "???";
    }
  }

  public void printSelectedKey(SelectionKey key) {
    SelectableChannel channel = key.channel();
    if (channel instanceof ServerSocketChannel) {
      System.out.println("\tServerSocketChannel can perform : " + possibleActionsToString(key));
    } else {
      SocketChannel sc = (SocketChannel) channel;
      System.out
        .println("\tClient " + remoteAddressToString(sc) + " can perform : "
            + possibleActionsToString(key));
    }
  }

  private String possibleActionsToString(SelectionKey key) {
    if (!key.isValid()) {
      return "CANCELLED";
    }
    ArrayList<String> list = new ArrayList<>();
    if (key.isAcceptable())
      list.add("ACCEPT");
    if (key.isReadable())
      list.add("READ");
    if (key.isWritable())
      list.add("WRITE");
    return String.join(" and ", list);
  }
}
