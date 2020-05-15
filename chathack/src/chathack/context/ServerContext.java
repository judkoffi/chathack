package chathack.context;

import java.nio.channels.SelectionKey;
import chathack.ServerChatHack;
import chathack.common.reader.FrameReader;
import chathack.common.reader.IReader;
import chathack.frame.AnonymousConnection;
import chathack.frame.AuthentificatedConnection;
import chathack.frame.BroadcastMessage;
import chathack.frame.DirectMessage;
import chathack.frame.IFrame;
import chathack.frame.IFrameVisitor;

/**
 * ServerContext implement IFrameVisitor ==> pas besoin du this du context du coup
 */
public class ServerContext extends BaseContext implements IFrameVisitor {
  private final FrameReader reader = new FrameReader();
  private final ServerChatHack server;

  public ServerContext(SelectionKey key, ServerChatHack server) {
    super(key);
    this.server = server;
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
  public void visit(BroadcastMessage message) {
    server.broadcast(message.toBuffer());
  }

  @Override
  public void visit(DirectMessage directMessage) {
    // TODO Auto-generated method stub

  }


  @Override
  public void visit(AnonymousConnection message) {
    boolean availableLogin = server.registerAnnonymousClient(message.getLogin(), key);
    if (!availableLogin) {
      System.out.println("pas libre");
      silentlyClose();
      return;
    }
    System.out.println("libre");
  }


  @Override
  public void visit(AuthentificatedConnection message) {
    server.registerAuthenticatedClient(message);
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
