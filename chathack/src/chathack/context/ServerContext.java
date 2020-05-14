package chathack.context;

import java.nio.channels.SelectionKey;
import chathack.common.reader.FrameReader;
import chathack.common.reader.IReader;
import chathack.frame.IFrame;
import chathack.frame.ServerFrameVisitor;

public class ServerContext extends BaseContext {
  private final FrameReader reader = new FrameReader();

  //private final ServerFrameVisitor serverFrameVisitor = new ServerFrameVisitor(context, server)

  public ServerContext(SelectionKey key) {
    super(key);
  }

  private void handler(IFrame frame) {
    //frame.accept(frameVisitor);
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
    // TODO Auto-generated method stub

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
