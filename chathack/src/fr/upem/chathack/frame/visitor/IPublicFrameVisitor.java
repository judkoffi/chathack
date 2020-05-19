package fr.upem.chathack.frame.visitor;

import fr.upem.chathack.frame.AcceptPrivateConnection;
import fr.upem.chathack.frame.AnonymousConnection;
import fr.upem.chathack.frame.AuthentificatedConnection;
import fr.upem.chathack.frame.BroadcastMessage;
import fr.upem.chathack.frame.RejectPrivateConnection;
import fr.upem.chathack.frame.RequestPrivateConnection;
import fr.upem.chathack.frame.ServerResponseMessage;

public interface IPublicFrameVisitor extends IFrameVisitor {
  public void visit(AnonymousConnection message);

  public void visit(AuthentificatedConnection message);

  public void visit(BroadcastMessage message);

  public void visit(ServerResponseMessage serverMessage);

  public void visit(RequestPrivateConnection requestMessage);

  public void visit(AcceptPrivateConnection responsePrivateConnection);

  public void visit(RejectPrivateConnection rejectPrivateConnection);
}
