package fr.upem.chathack.frame;

public interface IFrameVisitor {

  public void visit(AnonymousConnection message);

  public void visit(AuthentificatedConnection message);

  public void visit(BroadcastMessage message);

  public void visit(DirectMessage directMessage);

  public void visit(ServerResponseMessage serverMessage);
  
  public void visit(RequestPrivateConnection requestMessage);
}
