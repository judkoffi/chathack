package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.ClientChatHack;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.trame.FrameReader;
import fr.upem.chathack.frame.AnonymousConnection;
import fr.upem.chathack.frame.AuthentificatedConnection;
import fr.upem.chathack.frame.BroadcastMessage;
import fr.upem.chathack.frame.DirectMessage;
import fr.upem.chathack.frame.IFrame;
import fr.upem.chathack.frame.IFrameVisitor;

public class ClientContext extends BaseContext implements IFrameVisitor {
  private final FrameReader reader = new FrameReader();
  private final ClientChatHack client;
  
  public ClientContext(SelectionKey key, ClientChatHack client) {
    super(key);
    this.client = client;
  }

  private void handler(IFrame frame) {
     frame.accept(this); 
  }
  
  @Override
  public void processIn() {
    for (;;) {
      IReader.ProcessStatus status = reader.process(bbin);
      switch (status) {
        case DONE:
          var msg = reader.get();
          handler(msg);
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
    System.out.println("toto");

  }

  @Override
  public void visit(AuthentificatedConnection message) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(BroadcastMessage message) {
	  System.out.println("visit broadcast msg : " + message);

  }

  @Override
  public void visit(DirectMessage directMessage) {
    // TODO Auto-generated method stub

  }
}
