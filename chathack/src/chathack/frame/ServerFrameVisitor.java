package chathack.frame;

import chathack.ServerChatHack;
import chathack.context.ServerContext;

public class ServerFrameVisitor implements IFrameVisitor {
  private final ServerContext context;
  private final ServerChatHack server;


  public ServerFrameVisitor(ServerChatHack server, ServerContext context) {
    this.server = server;
    this.context = context;
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
    boolean availableLogin = server.registerClient(message.getLogin(), context.getKey());
    if (!availableLogin) {
      System.out.println("pas libre");
      context.silentlyClose();
      return;
    }
    System.out.println("libre");
  }


  @Override
  public void visit(AuthentificatedConnection message) {
    
  }
}
