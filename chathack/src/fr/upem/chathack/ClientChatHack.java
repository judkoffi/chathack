package fr.upem.chathack;

import static fr.upem.chathack.utils.Helper.getCurrentIp;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import fr.upem.chathack.common.model.Message;
import fr.upem.chathack.context.BaseContext;
import fr.upem.chathack.context.ClientContext;
import fr.upem.chathack.context.PrivateConnectionContext;
import fr.upem.chathack.context.PrivateConnectionInfo;
import fr.upem.chathack.frame.AcceptPrivateConnection;
import fr.upem.chathack.frame.AnonymousConnection;
import fr.upem.chathack.frame.AuthentificatedConnection;
import fr.upem.chathack.frame.BroadcastMessage;
import fr.upem.chathack.frame.DirectMessage;
import fr.upem.chathack.frame.RejectPrivateConnection;
import fr.upem.chathack.frame.RequestPrivateConnection;

public class ClientChatHack {
  private static final Logger logger = Logger.getLogger(ClientChatHack.class.getName());
  private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
  private final HashMap<String, PrivateConnectionInfo> alreadyConnection = new HashMap<>();

  private final SocketChannel sc;
  private final Selector selector;
  private final InetSocketAddress serverAddress;
  private ClientContext uniqueContext;
  private final String login;
  private String password;
  private final Thread console;
  private final ArrayList<RequestPrivateConnection> pendingPrivateRequests;
  private ServerSocketChannel serverSocketChannel;

  public ClientChatHack(InetSocketAddress serverAddress, String path, String login)
      throws IOException {
    this.serverAddress = serverAddress;
    this.login = login;
    this.sc = SocketChannel.open();
    this.serverSocketChannel = ServerSocketChannel.open();
    this.selector = Selector.open();
    this.console = new Thread(this::consoleRun);
    this.pendingPrivateRequests = new ArrayList<>();
  }

  public ClientChatHack(InetSocketAddress serverAddress, String path, String login, String password)
      throws IOException {
    this(serverAddress, path, login); // call the other constructeur
    this.password = password;
  }

  private void consoleRun() {
    try (Scanner scan = new Scanner(System.in)) {
      while (scan.hasNextLine()) {
        var msg = scan.nextLine();
        sendCommand(msg);
      }
    } catch (InterruptedException e) {
      logger.info("Console thread has been interrupted");
    } finally {
      logger.info("Console thread stopping");
    }
  }

  private void sendCommand(String msg) throws InterruptedException {
    synchronized (commandQueue) {
      this.commandQueue.put(msg);
      selector.wakeup();
    }
  }

  public boolean havePrivateConnection(String destinator) {
    return alreadyConnection.containsKey(destinator);
  }

  private void processPrefixBySlash(String line) {
    if (line.startsWith("/file")) {
      System.out.println("send files");
      return;
    }

    if (line.startsWith("/requests")) {
      if (pendingPrivateRequests.isEmpty()) {
        System.out.println("No pending private request");
        return;
      }
      pendingPrivateRequests.forEach(System.out::println);
      return;
    }

    var isAcceptCommand = line.startsWith("/accept");
    var isRejectCommand = line.startsWith("/reject");

    var splited = line.split(" ");
    if (splited.length < 2) {
      if (isAcceptCommand)
        System.err.println("usage: /accept login");

      if (isRejectCommand)
        System.err.println("usage: /reject login");
      return;
    }

    var fromLogin = splited[1];
    if (fromLogin.equals(login))
      return;

    boolean havePending = pendingPrivateRequests
      .stream()
      .anyMatch(p -> p.getFromLogin().getValue().equals(fromLogin));

    if (!havePending)
      return;

    var targetRequest = pendingPrivateRequests
      .stream()
      .filter(p -> p.getFromLogin().getValue().equals(fromLogin))
      .findFirst();

    if (isAcceptCommand) {
      var addr = new InetSocketAddress(getCurrentIp(), serverSocketChannel.socket().getLocalPort());
      var acceptMsg = new AcceptPrivateConnection(fromLogin, login, addr);
      uniqueContext.queueMessage(acceptMsg.toBuffer());
      targetRequest.ifPresent(pendingPrivateRequests::remove);
    }

    if (isRejectCommand) {
      var rejectMsg = new RejectPrivateConnection(fromLogin, login);
      uniqueContext.queueMessage(rejectMsg.toBuffer());
      targetRequest.ifPresent(pendingPrivateRequests::remove);
    }
  }


  private void processPrefixByAlt(String line) {
    // @login msg -> login msg
    var splited = line.substring(1).split(" ");
    if (splited.length < 2) {
      System.err.println("usage: @targetLogin msg");
      return;
    }

    var targetLogin = splited[0];
    var message = splited[1];
    if (havePrivateConnection(targetLogin)) {
      var dmMsg = new DirectMessage(login, targetLogin, message);
      alreadyConnection.get(targetLogin).getContext().queueMessage(dmMsg.toBuffer());
    } else {
      var dmRequest = new RequestPrivateConnection(login, targetLogin);
      if (pendingPrivateRequests.contains(dmRequest) || targetLogin.equals(login))
        return;
      this.uniqueContext.queueMessage(dmRequest.toBuffer());
    }
  }


  private void processCommands() {
    for (;;) {
      synchronized (commandQueue) {
        var line = this.commandQueue.poll();
        if (line == null) {
          return;
        }
        if (line.isBlank())
          return;

        switch (line.charAt(0)) {
          case '/': {
            processPrefixBySlash(line);
            break;
          }
          case '@': {
            processPrefixByAlt(line);
            break;
          }
          default: {
            Message msg = new Message(this.login, line);
            this.uniqueContext.queueMessage(new BroadcastMessage(msg).toBuffer());
            break;
          }
        }
      }
    }
  }

  public void putAlreadyConnection(String login, PrivateConnectionInfo privateConnectionInfo) {
    alreadyConnection.put(login, privateConnectionInfo);
  }

  public void addPrivateConnectionRequest(RequestPrivateConnection requestMessage) {
    if (!pendingPrivateRequests.contains(requestMessage))
      pendingPrivateRequests.add(requestMessage);
  }

  private void doAccept(SelectionKey key) throws IOException {
    SocketChannel sc = serverSocketChannel.accept();
    if (sc == null)
      return; // the selector gave a bad hint
    sc.configureBlocking(false);
    System.out.println("hhoohoo");
    SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    var ctx = new PrivateConnectionContext(clientKey, this);
    // map.put(targetLogin, new PrivateConnectionInfo(ctx));
    // send discover paquet
    clientKey.attach(ctx);
  }

  public void doConnectionWithClient(InetSocketAddress targetAddr, String targetLogin) {
    try {
      var socket = SocketChannel.open();
      socket.configureBlocking(false);
      var key = socket.register(selector, SelectionKey.OP_CONNECT);
      var ctx = new PrivateConnectionContext(key, this);
      key.attach(ctx);
      alreadyConnection.put(targetLogin, new PrivateConnectionInfo(ctx));
      System.out.println(alreadyConnection);
      socket.connect(targetAddr);
    } catch (IOException e) {
      System.err.println("Failed to connect incomming client");
    }
  }


  public void launch() throws IOException {
    // binder un port random
    serverSocketChannel.bind(new InetSocketAddress(0));
    serverSocketChannel.configureBlocking(false);
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

    sc.configureBlocking(false);
    var key = sc.register(selector, SelectionKey.OP_CONNECT);
    uniqueContext = new ClientContext(key, this);
    key.attach(uniqueContext);
    sc.connect(serverAddress);

    /**
     * When, client connected, send anonymous or authenticated request to connect client with server
     */

    var request = this.password == null //
        ? new AnonymousConnection(login)
        : new AuthentificatedConnection(login, password);

    this.uniqueContext.putInQueue(request.toBuffer());
    console.start();// run stdin thread

    while (!Thread.interrupted()) {
      // printKeys();
      try {
        selector.select(this::treatKey);
        processCommands();
      } catch (UncheckedIOException tunneled) {
        throw tunneled.getCause();
      }
    }
  }

  private void treatKey(SelectionKey key) {
    // printSelectedKey(key);
    try {
      if (key.isValid() && key.isAcceptable()) {
        doAccept(key);
      }
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }

    try {
      if (key.isValid() && key.isConnectable()) {
        if (key.attachment() instanceof ClientContext) {
          ((ClientContext) key.attachment()).doConnect();
        }
        if (key.attachment() instanceof PrivateConnectionContext) {
          ((PrivateConnectionContext) key.attachment()).doConnect();
        }
      }
      if (key.isValid() && key.isWritable()) {
        ((BaseContext) key.attachment()).doWrite();
      }
      if (key.isValid() && key.isReadable()) {
        ((BaseContext) key.attachment()).doRead();
      }
    } catch (IOException ioe) {
      // lambda call in select requires to tunnel IOException
      throw new UncheckedIOException(ioe);
    }
  }

  public void interruptConsole() {
    this.console.interrupt();
  }


  public String getLogin() {
    return login;
  }

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

  private static void usage() {
    System.out.println("usage : Client hostname port pathToDirectory login password (optional)");
  }

  public static void main(String[] args) throws NumberFormatException, IOException {
    if (args.length < 4 || args.length > 5) {
      usage();
      return;
    }

    var srvAddr = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
    ClientChatHack client = null;

    // IP address server ,nb port, path, login
    if (args.length == 4) {
      client = new ClientChatHack(srvAddr, args[2], args[3]);
    } else {
      // IP address server ,nb port, path, login, password
      client = new ClientChatHack(srvAddr, args[2], args[3], args[4]);
    }

    System.out.println("connection to server");
    client.launch();
  }
}
