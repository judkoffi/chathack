package chathack.context;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import chathack.Client;
import chathack.common.model.Message;
import chathack.common.reader.FrameReader;
import chathack.common.reader.IReader;
import chathack.common.reader.MessageReader;
import chathack.frame.AnonymousConnection;
import chathack.frame.AuthentificatedConnection;
import chathack.frame.BroadcastMessage;
import chathack.frame.DirectMessage;
import chathack.frame.IFrame;
import chathack.frame.IFrameVisitor;

public class ClientContext extends BaseContext implements IFrameVisitor {
  private final MessageReader messageReader = new MessageReader();
  private final FrameReader reader = new FrameReader();
  private final Client client;
  
  public ClientContext(SelectionKey key, Client client) {
    super(key);
    this.client = client;
  }

  private void handler(IFrame frame) {
	 frame.accept(this); //pas sur
  }
  
  @Override
  public void processIn() {
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

  @Override
  public void processOut() {
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

  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return;
    }

    updateInterestOps();
  }

  @Override
  public void visit(AnonymousConnection message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(AuthentificatedConnection message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(BroadcastMessage message) {
    

  }

  @Override
  public void visit(DirectMessage directMessage) {
    // TODO Auto-generated method stub

  }
}
