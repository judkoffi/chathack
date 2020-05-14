package chathack.context;

import static chathack.utils.Helper.BUFFER_SIZE;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public abstract class BaseContext implements IContext {
  protected final SelectionKey key;
  protected final SocketChannel sc;
  protected final ByteBuffer bbin = ByteBuffer.allocate(BUFFER_SIZE);
  protected final ByteBuffer bbout = ByteBuffer.allocate(BUFFER_SIZE);
  protected final Queue<ByteBuffer> queue = new LinkedList<>();
  protected boolean closed = false;

  public BaseContext(SelectionKey key) {
    this.key = key;
    this.sc = (SocketChannel) key.channel();
  }

  public SelectionKey getKey() {
    return key;
  }

  @Override
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

  /**
   * Performs the read action on sc
   *
   * The convention is that both buffers are in write-mode before the call to doRead and after the
   * call
   *
   * @throws IOException
   */
  @Override
  public void doRead() throws IOException {
    var read = sc.read(bbin);
    if (read == -1)
      closed = true;
    processIn();
    updateInterestOps();
  }

  /**
   * Performs the write action on sc
   *
   * The convention is that both buffers are in write-mode before the call to doWrite and after the
   * call
   *
   * @throws IOException
   */
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
