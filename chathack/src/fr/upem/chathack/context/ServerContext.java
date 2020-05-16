package fr.upem.chathack.context;

import java.nio.channels.SelectionKey;
import fr.upem.chathack.ServerChatHack;
import fr.upem.chathack.common.reader.FrameReader;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.frame.AnonymousConnection;
import fr.upem.chathack.frame.AuthentificatedConnection;
import fr.upem.chathack.frame.BroadcastMessage;
import fr.upem.chathack.frame.DirectMessage;
import fr.upem.chathack.frame.IFrame;
import fr.upem.chathack.frame.IFrameVisitor;

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
      System.out.println("AnonymousConnection login not available");
      silentlyClose();
      return;
    }
    System.out.println("AnonymousConnection login available");
  }


  @Override
  public void visit(AuthentificatedConnection message) {
    if (!server.isAvailableLogin(message.getLogin())) {
      // Not available login
      System.out.println("AuthentificatedConnection login not available ");
      silentlyClose();
      return;
    }
    System.out.println("AuthentificatedConnection login available ");
    server.registerAuthenticatedClient(message, key);
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
