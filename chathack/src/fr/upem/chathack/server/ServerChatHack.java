package fr.upem.chathack.server;

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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.upem.chathack.context.BaseContext;
import fr.upem.chathack.context.DatabaseContext;
import fr.upem.chathack.dbframe.CheckCredentialMessage;
import fr.upem.chathack.dbframe.CheckLoginMessage;
import fr.upem.chathack.dbframe.DatabaseResponseMessage;
import fr.upem.chathack.publicframe.AuthentificatedConnection;
import fr.upem.chathack.publicframe.RequestPrivateConnection;
import fr.upem.chathack.publicframe.ServerResponseMessage;
import fr.upem.chathack.utils.Helper;
import fr.upem.chathack.utils.OpCode;

/**
 * Class use to represent a server of protocol ChatHack
 */
public class ServerChatHack {

  private static final Logger logger = Logger.getLogger(ServerChatHack.class.getName());
  private static final int TIMEOUT = 500; // 500 ms
  private final Selector selector;
  private final ServerSocketChannel serverSocketChannel;
  private final SocketChannel dbChannel;
  final HashMap<String, ClientInfo> map = new HashMap<>();
  private DatabaseContext databaseContext;
  private final InetSocketAddress databaseAddress;

  public ServerChatHack(int port, String dbHostname, int dbPort) throws IOException {
    Objects.requireNonNull(dbHostname);
    Objects.requireNonNull(port);
    Objects.requireNonNull(dbPort);
    serverSocketChannel = ServerSocketChannel.open();
    serverSocketChannel.bind(new InetSocketAddress(port));
    dbChannel = SocketChannel.open();
    databaseAddress = new InetSocketAddress(dbHostname, dbPort);
    dbChannel.connect(databaseAddress);
    selector = Selector.open();
  }

  /*
   * Find the highest id in map and increment with 1 to have new id
   */
  private long getNextMapId() {
    return map
      .entrySet()
      .stream()
      .max((e1, e2) -> e1.getValue().id > e2.getValue().id ? 1 : -1)
      .map(p -> p.getValue().id + 1)
      .orElse((long) 1); // map empty
  }

  /**
   * Send a message to a client by given his selection key
   * 
   * @param msg
   * @param key
   */
  public void sendMessageToClient(ByteBuffer msg, SelectionKey key) {
    findContextByKey(key).ifPresent(c -> c.queueMessage(msg));
  }


  public void broadcast(ByteBuffer bb) {
    selector
      .keys()
      .stream()
      .filter(SelectionKey::isValid)
      .filter(k -> k.attachment() != null)
      .filter(k -> !k.equals(databaseContext.getKey()))
      .forEach(k ->
      {
        var ctx = ((ServerContext) k.attachment());
        ctx.queueMessage(bb);
      });
  }


  public boolean isExistLogin(String login) {
    return map.containsKey(login);
  }

  public boolean isConnected(String login) {
    return map.get(login) != null && map.get(login).isAuthenticated;
  }

  public void sendPrivateConnectionRequest(RequestPrivateConnection request) {
    var target = request.getReceiver().getValue();
    map.get(target).context.queueMessage(request.toBuffer());
  }

  private Optional<ServerContext> findContextByKey(SelectionKey key) {
    return selector
      .keys()
      .stream()
      .filter(SelectionKey::isValid)
      .filter(k -> k.attachment() != null)
      .filter(k -> !k.equals(databaseContext.getKey()))
      .filter(k -> k.equals(key))
      .map(m -> (ServerContext) m.attachment())
      .findFirst();
  }

  public void removeClientByKey(SelectionKey key) {
    findLoginByKey(key).ifPresent(map::remove);
  }

  private Optional<String> findLoginByKey(SelectionKey key) {
    return map
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().key.equals(key))
      .map(Map.Entry::getKey)
      .findFirst();
  }

  /*****************************
   * Authentication methods
   ******************************/

  public void registerAnonymousClient(String login, SelectionKey clientKey) {
    map.put(login, new ClientInfo(true, clientKey, getNextMapId()));
    var msg = new CheckLoginMessage(login, map.get(login).id);
    databaseContext.queueMessage(msg.toBuffer());
  }

  public void registerAuthenticatedClient(AuthentificatedConnection message, SelectionKey key) {
    var login = message.getLogin().getValue();
    map.put(login, new ClientInfo(false, key, getNextMapId()));
    var id = map.get(login).id;
    var msg = new CheckCredentialMessage(message.getLogin(), message.getPassword(), id);
    databaseContext.queueMessage(msg.toBuffer());
  }

  public void responseCheckLogin(DatabaseResponseMessage trame) {
    var clt = map.entrySet().stream().filter(p -> p.getValue().id == trame.getResult()).findFirst();
    if (clt.isEmpty())
      return;

    var entry = clt.get();
    var key = entry.getValue().key;
    findContextByKey(key).ifPresent(c -> handlerDbResponse(c, trame.getOpCode(), entry));
  }

  public void launch() throws IOException {
    logger.info("server running on port " + serverSocketChannel.socket().getLocalPort());
    serverSocketChannel.configureBlocking(false);
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    dbConnection();

    var lastCheck = System.currentTimeMillis();
    while (!Thread.interrupted()) {
      // System.out.println("Starting select");
      printKeys();
      try {
        selector.select(this::treatKey);
        var currentTime = System.currentTimeMillis();
        if (currentTime >= lastCheck + TIMEOUT) {
          lastCheck = currentTime;
          clearInvalidSelectionKey();
        }
        System.out.println(map);
      } catch (UncheckedIOException tunneled) {
        throw tunneled.getCause();
      }
    }
  }

  /**
   * Remove in map private connection with invalid key
   */
  private void clearInvalidSelectionKey() {
    for (var key : selector.keys()) {
      if (!key.isValid()) {
        var target = map.entrySet().stream().filter(p -> p.getValue().key.equals(key)).findFirst();
        target.ifPresent(t ->
        {
          t.getValue().context.silentlyClose();
          map.remove(t.getKey());
        });
      }
    }
  }

  private void dbConnection() throws IOException {
    dbChannel.configureBlocking(false);
    var dbKey = dbChannel.register(selector, SelectionKey.OP_READ);
    databaseContext = new DatabaseContext(dbKey, this);
    dbKey.attach(databaseContext);
  }

  private void handlerDbResponse(ServerContext c, byte b, Entry<String, ClientInfo> entry) {
    ServerResponseMessage msg = null;
    switch (b) {
      /**
       * DB respose trame opcde:<br>
       * 1 -> valid response <br>
       * 0 -> invalid response
       */

      case OpCode.DB_INVALID_RESPONSE: {
        // Login free in db
        if (map.get(entry.getKey()).anonymous) {
          map.get(entry.getKey()).isAuthenticated = true;
          map.get(entry.getKey()).context = c;
        }

        msg = map.get(entry.getKey()).isAuthenticated
            ? new ServerResponseMessage(Helper.WELCOME_MESSAGE, false)
            : new ServerResponseMessage("Wrong credentials", true);
        c.queueMessage(msg.toBuffer());
        break;
      }
      case OpCode.DB_VALID_RESPONSE: {
        // good credentials
        if (!map.get(entry.getKey()).anonymous) {
          map.get(entry.getKey()).isAuthenticated = true;
          map.get(entry.getKey()).context = c;
        }

        msg = map.get(entry.getKey()).isAuthenticated
            ? new ServerResponseMessage(Helper.WELCOME_MESSAGE, false)
            : new ServerResponseMessage("Not available login", true);

        c.queueMessage(msg.toBuffer());
        break;
      }
      default:
        throw new IllegalArgumentException("unknown db response byte" + b);
    }

    if (!map.get(entry.getKey()).isAuthenticated) {
      try {
        c.doWrite();// force writing msg
      } catch (IOException e) {
        //
      }
      map.remove(entry.getKey());
      c.silentlyClose();
    }
  }

  private void treatKey(SelectionKey key) {
    printSelectedKey(key);
    try {
      if (key.isValid() && key.isAcceptable()) {
        doAccept(key);
      }
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }

    try {
      if (key.isValid() && key.isWritable()) {
        ((BaseContext) key.attachment()).doWrite();
      }
      if (key.isValid() && key.isReadable()) {
        ((BaseContext) key.attachment()).doRead();
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

  private void printKeys() {
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

  private void printSelectedKey(SelectionKey key) {
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
