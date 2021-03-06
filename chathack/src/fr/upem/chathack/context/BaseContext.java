package fr.upem.chathack.context;

import static fr.upem.chathack.utils.Helper.BUFFER_SIZE;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Objects;

/**
 * Abstract class use to factorize common methods of different context
 */
public abstract class BaseContext implements IContext {
  protected final SelectionKey key;
  protected final SocketChannel sc;
  protected final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
  protected final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
  protected final ArrayDeque<ByteBuffer> queue = new ArrayDeque<>(); // borner la queue
  protected boolean closed = false;

  public BaseContext(SelectionKey key) {
    this.key = key;
    this.sc = (SocketChannel) key.channel();
  }

  /**
   * Getter of current context selection key
   * 
   * @return: current context selection key
   */
  public SelectionKey getKey() {
    return key;
  }

  @Override
  public void processOut() {
    while (!queue.isEmpty()) {
      var bb = queue.peek();
      if (bbout.remaining() < bb.remaining())
        return;
      queue.remove();
      bbout.put(bb);
    }
  }

  @Override
  public void updateInterestOps() {
    if (!key.isValid()) {
      return;
    }

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

  @Override
  public void doRead() throws IOException {
    var read = sc.read(bbin);
    if (read == -1)
      closed = true;
    processIn();
    updateInterestOps();
  }

  @Override
  public void doWrite() throws IOException {
    bbout.flip();
    sc.write(bbout);
    bbout.compact();
    processOut();
    updateInterestOps();
  }

  @Override
  public void queueMessage(ByteBuffer bb) {
    queue.add(bb);
    processOut();
    updateInterestOps();
  }

  @Override
  public void silentlyClose() {
    try {
      sc.close();
    } catch (IOException e) {
      // ignore exception
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof BaseContext))
      return false;

    BaseContext context = (BaseContext) obj;
    return context.key.equals(key) && context.sc.equals(sc);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, sc);
  }
}
