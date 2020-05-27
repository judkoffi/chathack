package fr.upem.chathack.client;

import static fr.upem.chathack.model.PrivateConnectionInfo.PrivateConnectionState.WAITING_COMFIRM_TOKEN;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import fr.upem.chathack.context.BaseContext;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.Message;
import fr.upem.chathack.model.PrivateConnectionInfo;
import fr.upem.chathack.privateframe.DirectMessage;
import fr.upem.chathack.privateframe.FileMessage;
import fr.upem.chathack.publicframe.AcceptPrivateConnection;
import fr.upem.chathack.publicframe.AnonymousConnection;
import fr.upem.chathack.publicframe.AuthentificatedConnection;
import fr.upem.chathack.publicframe.BroadcastMessage;
import fr.upem.chathack.publicframe.LogOutMessage;
import fr.upem.chathack.publicframe.RejectPrivateConnection;
import fr.upem.chathack.publicframe.RequestPrivateConnection;

/**
 * 
 * Class use to create client with protocole Chaton
 */
public class ClientChatHack {
  // Map use to keep information about private connection between clients
  final Map<String, PrivateConnectionInfo> privateConnectionMap = new HashMap<>();

  // Map use to keep information about pending private connection between clients
  final ArrayList<RequestPrivateConnection> pendingPrivateRequests = new ArrayList<>();

  private static final Logger logger = Logger.getLogger(ClientChatHack.class.getName());
  private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);

  private final SocketChannel sc;
  private final Selector selector;
  private final InetSocketAddress serverAddress;
  private ClientContext uniqueContext;
  private String password;
  private final Thread console;
  private ServerSocketChannel serverSocketChannel;

  final String login;
  final String path;

  public ClientChatHack(InetSocketAddress serverAddress, String path, String login)
      throws IOException {
    this.serverAddress = Objects.requireNonNull(serverAddress);
    this.login = Objects.requireNonNull(login);
    this.path = Objects.requireNonNull(path);
    checkPath(path);
    this.sc = SocketChannel.open();
    this.serverSocketChannel = ServerSocketChannel.open();
    this.selector = Selector.open();
    this.console = new Thread(this::consoleRun);
  }

  /**
   * Method use to check if path as client directory use to store shared files give on command line
   * is valid
   * 
   * @param path
   */
  private void checkPath(String path) {
    Path file = new File(path).toPath();
    boolean exists = Files.exists(file); // Check if the file exists
    boolean isDirectory = Files.isDirectory(file); // Check if it's a directory
    if (!exists || !isDirectory)
      throw new IllegalArgumentException(path + " must be exist and a directory");
  }

  public ClientChatHack(InetSocketAddress serverAddress, String path, String login, String password)
      throws IOException {
    this(serverAddress, path, login); // call the other constructeur
    this.password = Objects.requireNonNull(password);
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

  private ByteBuffer readFile(String filename) {
    try (var reader = new RandomAccessFile(filename, "r"); var channel = reader.getChannel()) {
      int bufferSize = (int) channel.size();
      ByteBuffer buff = ByteBuffer.allocate(bufferSize);
      channel.read(buff);
      buff.flip();
      return buff;
    } catch (FileNotFoundException e) {
      logger.info(filename + " file does not exist");
    } catch (IOException e) {
      logger.info(e.getMessage());
    }
    return null;
  }


  private void fileHandler(String line) {
    var splited = line.split(" ");
    if (splited.length < 3) {
      System.err.println("usage: /file login filename");
      return;
    }
    var receiver = splited[1];
    var filename = splited[2];
    var filePath = Path.of(splited[2]);
    if (!Files.exists(filePath)) {
      System.err.println(splited[2] + " file does not exist.");
      return;
    }

    var fileContent = readFile(filename);
    var fileMsg = new FileMessage(filename, receiver, fileContent);
    if (!existPrivateConnection(receiver)) {
      sendPrivateConnectionRequest(receiver);
      privateConnectionMap.get(receiver).getMessageQueue().add(fileMsg.toBuffer());
      return;
    }
    handlerPrivateFrameSending(fileMsg, receiver);
  }


  private void requestHandler() {
    if (pendingPrivateRequests.isEmpty()) {
      System.out.println("No pending private request");
      return;
    }
    pendingPrivateRequests.forEach(System.out::println);
  }

  private void processPrefixedBySlash(String line) {
    if (line.startsWith("/file")) {
      fileHandler(line);
    }

    if (line.startsWith("/requests")) {
      requestHandler();
    }

    if (line.startsWith("/logout")) {
      logoutHandler();
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

    var receiver = splited[1];
    if (receiver.equals(login))// current user try to accept himself
      return;

    boolean havePending =
        pendingPrivateRequests.stream().anyMatch(p -> p.getAppliant().getValue().equals(receiver));

    if (!havePending)
      return;

    var targetRequest = pendingPrivateRequests
      .stream()
      .filter(p -> p.getAppliant().getValue().equals(receiver))
      .findFirst();

    if (isAcceptCommand) {
      var addr = new InetSocketAddress("localhost", serverSocketChannel.socket().getLocalPort());
      var token = new Random().nextLong();
      var acceptMsg = new AcceptPrivateConnection(receiver, login, addr, token);
      var info = new PrivateConnectionInfo(receiver, WAITING_COMFIRM_TOKEN, token);
      privateConnectionMap.put(receiver, info);
      uniqueContext.queueMessage(acceptMsg.toBuffer());
    }

    if (isRejectCommand) {
      var rejectMsg = new RejectPrivateConnection(receiver, login);
      uniqueContext.queueMessage(rejectMsg.toBuffer());
    }

    targetRequest.ifPresent(pendingPrivateRequests::remove);
  }


  private void logoutHandler() {
    var msg = new LogOutMessage(new Message(login, ""));
    uniqueContext.queueMessage(msg.toBuffer());
  }

  private boolean existPrivateConnection(String receiver) {
    return privateConnectionMap.containsKey(receiver);
  }

  private void sendPrivateConnectionRequest(String receiver) {
    var request = new RequestPrivateConnection(login, receiver);
    if (pendingPrivateRequests.contains(request) || receiver.equals(login))
      return;
    privateConnectionMap.put(receiver, new PrivateConnectionInfo(receiver));
    this.uniqueContext.queueMessage(request.toBuffer());
  }

  private void processPrefixedByAlt(String line) {
    // @login msg -> login msg
    var splited = line.substring(1).split(" ");
    if (splited.length < 2) {
      System.err.println("usage: @targetLogin msg");
      return;
    }

    var receiver = splited[0];
    var message = Arrays.stream(splited).skip(1).collect(Collectors.joining(" "));

    var dmMsg = new DirectMessage(login, receiver, message);
    if (!existPrivateConnection(receiver)) {
      sendPrivateConnectionRequest(receiver);
      // add first message
      privateConnectionMap.get(receiver).getMessageQueue().add(dmMsg.toBuffer());
      return;
    }
    handlerPrivateFrameSending(dmMsg, receiver);
  }

  private void handlerPrivateFrameSending(IPrivateFrame frame, String receiver) {
    var privateConnection = privateConnectionMap.get(receiver);
    switch (privateConnection.getState()) {
      case SUCCEED:
        privateConnectionMap.get(receiver).getDestinatorContext().queueMessage(frame.toBuffer());
        break;
      case PENDING:
      case WAITING_COMFIRM_TOKEN:
        privateConnectionMap.get(receiver).getMessageQueue().add(frame.toBuffer());
        break;
      default:
        throw new AssertionError();
    }
  }

  private void processCommands() {
    for (;;) {
      synchronized (commandQueue) {
        var line = this.commandQueue.poll();
        if (line == null || line.isBlank())
          return;

        switch (line.charAt(0)) {
          case '/': {
            processPrefixedBySlash(line);
            break;
          }
          case '@': {
            processPrefixedByAlt(line);
            break;
          }
          default: {
            var msg = new Message(this.login, line);
            this.uniqueContext.queueMessage(new BroadcastMessage(msg).toBuffer());
            break;
          }
        }
      }
    }
  }

  private void doAccept(SelectionKey key) throws IOException {
    SocketChannel sc = serverSocketChannel.accept();
    if (sc == null)
      return; // the selector gave a bad hint
    sc.configureBlocking(false);
    SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    var ctx = new PrivateConnectionContext(clientKey, this);
    clientKey.attach(ctx);
  }

  /**
   * Method use to make socket connection beetween two clients
   * 
   * @param response: a abject of @link {@link AcceptPrivateConnection} which contains IP and port
   *        to make conection
   */
  public void doConnectionWithClient(AcceptPrivateConnection response) {
    var receiver = response.getReceiver();
    var token = response.getToken();

    try {
      var socket = SocketChannel.open();
      socket.configureBlocking(false);
      var key = socket.register(selector, SelectionKey.OP_CONNECT);

      privateConnectionMap.get(receiver).setState(WAITING_COMFIRM_TOKEN);
      privateConnectionMap.get(receiver).setToken(token);
      var ctx = new PrivateConnectionContext(key, this, token, receiver);
      key.attach(ctx);
      privateConnectionMap.get(receiver).setDestinatorContext(ctx);
      socket.connect(response.getTargetAddress());
    } catch (IOException e) {
      logger.info("Failed to connect incomming client");
    }
  }

  private void initBinding() throws IOException {
    // binder un port random
    serverSocketChannel.bind(new InetSocketAddress(0));
    serverSocketChannel.configureBlocking(false);
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

    sc.configureBlocking(false);
    var key = sc.register(selector, SelectionKey.OP_CONNECT);
    uniqueContext = new ClientContext(key, this);
    key.attach(uniqueContext);
    sc.connect(serverAddress);
  }

  /**
   * Method use to launch a client
   * 
   * @throws IOException
   */
  public void launch() throws IOException {
    initBinding();
    var request = this.password == null ? new AnonymousConnection(login)
        : new AuthentificatedConnection(login, password);

    this.uniqueContext.putInQueue(request.toBuffer());
    console.setDaemon(true);
    console.start();// run stdin thread

    while (!Thread.interrupted()) {
      // printKeys();
      try {
        if (uniqueContext.isClosed()) {
          console.interrupt();
          selector.close();
          return;
        }
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
    client.launch();
  }

}
