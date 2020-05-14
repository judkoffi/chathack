package chathack.frame;

public interface IFrameVisitor {

  public void visit(AnonymousConnection message);

  public void visit(BroadcastMessage message);

  public void visit(DirectMessage directMessage);
}
