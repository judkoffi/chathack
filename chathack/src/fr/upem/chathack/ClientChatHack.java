package fr.upem.chathack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import fr.upem.chathack.common.model.BiString;
import fr.upem.chathack.context.ClientContext;

public class ClientChatHack {

  private static final Logger logger = Logger.getLogger(ClientChatHack.class.getName());

  private final SocketChannel sc;
  private final Selector selector;
  private final InetSocketAddress serverAddress;
  private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
  private ClientContext uniqueContext;
  private final String login;
  private String password;
  private Thread console = new Thread(this::consoleRun);

  public ClientChatHack(InetSocketAddress serverAddress, String path, String login)
      throws IOException {
    this.serverAddress = serverAddress;
    this.login = login;
    this.sc = SocketChannel.open();
    this.selector = Selector.open();
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

  private void processCommands() {
    for (;;) {
      synchronized (commandQueue) {
        var line = this.commandQueue.poll();
        if (line == null) {
          return;
        }

        switch (line.charAt(0)) {
          case '/': {
            System.out.println("send files");
            break;
          }
          case '@': {
            System.out.println("private message");
            break;
          }
          default: {
            System.out.println("public message");
            this.uniqueContext.queueMessage(new BiString(login, line).toBuffer());
            return;
          }
        }
      }
    }
  }

  public void launch() throws IOException {
    sc.configureBlocking(false);
    var key = sc.register(selector, SelectionKey.OP_CONNECT);
    uniqueContext = new ClientContext(key, this);
    key.attach(uniqueContext);
    sc.connect(serverAddress);

    /**
     * When, client connected, send anonymous or authenticated request to connect client with server
     */

    console.start();// run stdin thread

    while (!Thread.interrupted()) {
      try {
        selector.select(this::treatKey);
        processCommands();
      } catch (UncheckedIOException tunneled) {
        throw tunneled.getCause();
      }
    }
  }

  private void treatKey(SelectionKey key) {
    try {
      if (key.isValid() && key.isConnectable()) {
        uniqueContext.doConnect();
      }
      if (key.isValid() && key.isWritable()) {
        uniqueContext.doWrite();
      }
      if (key.isValid() && key.isReadable()) {
        uniqueContext.doRead();
      }
    } catch (IOException ioe) {
      // lambda call in select requires to tunnel IOException
      throw new UncheckedIOException(ioe);
    }
  }

  private void silentlyClose(SelectionKey key) {
    try {
      key.channel().close();
    } catch (IOException e) {
      // ignore exception
    }
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
