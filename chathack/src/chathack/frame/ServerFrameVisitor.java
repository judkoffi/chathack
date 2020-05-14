package chathack.frame;

import chathack.ServerChatHack;
import chathack.context.ServerContext;

public class ServerFrameVisitor implements IFrameVisitor {
  private final ServerContext context;
  private final ServerChatHack server;

  public ServerFrameVisitor(ServerContext context, ServerChatHack server) {
    this.context = context;
    this.server = server;
  }

  public ServerContext getContext() {
    return context;
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
    server.registerClient(message.getLogin());
  }

}
