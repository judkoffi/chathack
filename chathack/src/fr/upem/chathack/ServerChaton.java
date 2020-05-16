package fr.upem.chathack;

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
import fr.upem.chathack.common.model.Message;
import fr.upem.chathack.common.reader.MessageReader;



public class ServerChaton {

  static private class Context {

    final private SelectionKey key;
    final private SocketChannel sc;
    final private ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
    final private ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
    final private Queue<ByteBuffer> queue = new LinkedList<>();
    private MessageReader messageReader = new MessageReader();
    final private ServerChaton server;
    private boolean closed = false;


    private Context(ServerChaton server, SelectionKey key) {
      this.key = key;
      this.sc = (SocketChannel) key.channel();
      this.server = server;
    }

    /**
     * Process the content of bbin
     *
     * The convention is that bbin is in write-mode before the call to process and after the call
     *
     */
    private void processIn() {
      for (;;) {
        var status = messageReader.process(bbin);
        switch (status) {
          case ERROR:
            silentlyClose();
            return;

          case REFILL:
            return;

          case DONE:
            var msg = messageReader.get();
            server.broadcast(msg); // transmettre message a tous les clients connectes
            messageReader.reset();
            break;
        }
      }
    }

    /**
     * Add a message to the message queue, tries to fill bbOut and updateInterestOps
     *
     * @param msg
     */
    private void queueMessage(ByteBuffer bb) {
      queue.add(bb);
      processOut();
      updateInterestOps();
    }

    /**
     * Try to fill bbout from the message queue
     *
     */
    private void processOut() {
      while (!queue.isEmpty()) {
        var bb = queue.peek();
        if (bbout.remaining() < bb.remaining())
          return;
        queue.remove();
        bbout.put(bb);
      }
    }

    /**
     * Update the interestOps of the key looking only at values of the boolean closed and of both
     * ByteBuffers.
     *
     * The convention is that both buffers are in write-mode before the call to updateInterestOps
     * and after the call. Also it is assumed that process has been be called just before
     * updateInterestOps.
     */

    private void updateInterestOps() {
      if (!key.isValid())
        return;
      var interestOps = 0;
      if (!closed && bbin.hasRemaining()) {
        interestOps = interestOps | SelectionKey.OP_READ;
      }
      if (bbout.position() != 0 || !queue.isEmpty()) { // on s'assure que il y a de quoi ecrire, en
                                                       // vérifiant s'il y a des éléments dans la
                                                       // queue
        interestOps = interestOps | SelectionKey.OP_WRITE;
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

    /**
     * Performs the read action on sc
     *
     * The convention is that both buffers are in write-mode before the call to doRead and after the
     * call
     *
     * @throws IOException
     */
    private void doRead() throws IOException {
      var read = sc.read(bbin);
      if (read == -1)
        closed = true;
      processIn();
      updateInterestOps();
    }

    /**
     * Performs the write action on sc
     *
     * The convention is that both buffers are in write-mode before the call to doWrite and after
     * the call
     *
     * @throws IOException
     */

    private void doWrite() throws IOException {
      bbout.flip();
      sc.write(bbout);
      bbout.compact();
      processOut();
      updateInterestOps();
    }

  }

  static private int BUFFER_SIZE = 1_024;
  static private Logger logger = Logger.getLogger(ServerChatHack.class.getName());

  private final ServerSocketChannel serverSocketChannel;
  private final Selector selector;

  public ServerChaton(int port) throws IOException {
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
      // lambda call in select requires to tunnel IOException
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
    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
    SocketChannel socketChannel = ssc.accept();
    if (socketChannel == null)
      return;
    socketChannel.configureBlocking(false);
    SelectionKey clientKey = socketChannel.register(selector, SelectionKey.OP_READ);
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

  /**
   * Add a message to all connected clients queue
   *
   * @param msg
   */
  private void broadcast(Message msg) {
    for (SelectionKey key : selector.keys()) {
      var cntxt = (Context) key.attachment();
      if (cntxt == null) {
        continue;
      }
      cntxt.queueMessage(msg.toBuffer());
    }
  }

  public static void main(String[] args) throws NumberFormatException, IOException {
    if (args.length != 1) {
      usage();
      return;
    }
    new ServerChaton(Integer.parseInt(args[0])).launch();
  }

  private static void usage() {
    System.out.println("Usage : ServerSumBetter port");
  }

}
