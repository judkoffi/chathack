package chathack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import chathack.common.model.Message;
import chathack.common.reader.IReader;
import chathack.common.reader.MessageReader;


public class Client {

  private static class Context {
    private final SelectionKey key;
    private final SocketChannel sc;
    private final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
    private final Queue<ByteBuffer> queue = new LinkedList<>(); // buffers read-mode
    private final MessageReader messageReader = new MessageReader();
    private boolean closed = false;

    private Context(SelectionKey key) {
      this.key = key;
      this.sc = (SocketChannel) key.channel();

    }


    private void processIn() {
      for (;;) {
        IReader.ProcessStatus status = messageReader.process(bbin);
        switch (status) {
          case DONE:
            Message msg = messageReader.get();
            messageReader.reset();
            System.out.println("message read : " + msg);
            break;
          case REFILL:
            return;
          case ERROR:
            silentlyClose();
            return;
        }
      }
    }

    private void queueMessage(ByteBuffer bb) {
      queue.add(bb);
      processOut();
      updateInterestOps();
    }

    private void processOut() {
      while (!queue.isEmpty()) {
        var bb = queue.peek();
        if (bb.remaining() <= bbout.remaining()) {
          queue.remove();
          bbout.put(bb);
        } else {
          return;
        }
      }
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
      if (sc.read(bbin) == -1) {
        closed = true;
      }
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

    public void doConnect() throws IOException {
      if (!sc.finishConnect()) {
        return;
      }

      updateInterestOps();
    }

  }

  /*********************************************************************************/

  private static final int BUFFER_SIZE = 4096;
  private static final Logger logger = Logger.getLogger(Client.class.getName());

  private final SocketChannel sc;
  private final Selector selector;
  private final InetSocketAddress serverAddress;
  private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(10);
  private Context uniqueContext;
  private final String login;
  private String password;
  private Thread console = new Thread(this::consoleRun);


  public Client(InetSocketAddress serverAddress, String path, String login) throws IOException {
    this.serverAddress = serverAddress;
    this.login = login;
    this.sc = SocketChannel.open();
    this.selector = Selector.open();

  }

  public Client(InetSocketAddress serverAddress, String path, String login, String password)
      throws IOException {
    this(serverAddress, path, login); // call the other constructeur
    this.password = password;

  }

  private void consoleRun() {
    try {
      Scanner scan = new Scanner(System.in);
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
          case '/':
            System.out.println("send files");
            break;

          case '@':
            System.out.println("private message");
            break;

          default:
            System.out.println("public message");
            return;
        }

        this.uniqueContext.queueMessage(new Message(login, line).toBuffer());
      }
    }
  }

  public void launch() throws IOException {
    sc.configureBlocking(false);
    var key = sc.register(selector, SelectionKey.OP_CONNECT);
    uniqueContext = new Context(key);
    key.attach(uniqueContext);
    sc.connect(serverAddress);

    console.start();

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
    Channel sc = (Channel) key.channel();
    try {
      sc.close();
    } catch (IOException e) {
      // ignore exception
    }
  }

  private static void usage() {
    System.out.println("usage : Client hostname port pathToDirectory login password (optional)");
  }

  public static void main(String[] args) throws NumberFormatException, IOException {
    if (args.length == 4) {
      // IP address server ,nb port, path, login

      new Client(new InetSocketAddress(args[0], Integer.parseInt(args[1])), args[2], args[3])
        .launch();

    } else if (args.length == 5) {
      // IP address server ,nb port, path, login, password
      new Client(new InetSocketAddress(args[0], Integer.parseInt(args[1])), args[2], args[3],
          args[4]).launch();

    } else {
      usage();
      return;
    }

    System.out.println("connection to server");
  }
}
