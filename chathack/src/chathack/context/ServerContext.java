package chathack.context;

import java.nio.channels.SelectionKey;
import chathack.ServerChatHack;
import chathack.common.reader.FrameReader;
import chathack.common.reader.IReader;
import chathack.frame.IFrame;
import chathack.frame.ServerFrameVisitor;

public class ServerContext extends BaseContext {
  private final FrameReader reader = new FrameReader();
  private final ServerFrameVisitor serverFrameVisitor;

  public ServerContext(SelectionKey key, ServerChatHack server) {
    super(key);
    this.serverFrameVisitor = new ServerFrameVisitor(server, this);
  }

  private void handler(IFrame frame) {
    frame.accept(serverFrameVisitor);
  }

  @Override
  public void processIn() {
    for (;;) {
      IReader.ProcessStatus status = reader.process(bbin);
      switch (status) {
        case DONE:
          IFrame frame = reader.get();
          handler(frame);
          reader.reset();
          break;
        case REFILL:
          return;
        case ERROR:
          silentlyClose();
          return;
      }
    }
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
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
