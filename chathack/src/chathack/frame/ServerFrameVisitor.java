package chathack.frame;

import chathack.ServerChatHack;

public class ServerFrameVisitor implements IFrameVisitor {
  private final ServerChatHack server;

  public ServerFrameVisitor(ServerChatHack server) {
    this.server = server;
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
