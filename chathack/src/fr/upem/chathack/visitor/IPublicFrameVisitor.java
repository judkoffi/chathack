package fr.upem.chathack.visitor;

import fr.upem.chathack.publicframe.AcceptPrivateConnection;
import fr.upem.chathack.publicframe.AnonymousConnection;
import fr.upem.chathack.publicframe.AuthentificatedConnection;
import fr.upem.chathack.publicframe.BroadcastMessage;
import fr.upem.chathack.publicframe.RejectPrivateConnection;
import fr.upem.chathack.publicframe.RequestPrivateConnection;
import fr.upem.chathack.publicframe.ServerResponseMessage;

public interface IPublicFrameVisitor extends IFrameVisitor {

  public default void visit(AnonymousConnection message) {}

  public default void visit(AuthentificatedConnection message) {}

  public void visit(BroadcastMessage message);

  public default void visit(ServerResponseMessage serverMessage) {}

  public void visit(RequestPrivateConnection requestMessage);

  public void visit(AcceptPrivateConnection responsePrivateConnection);

  public void visit(RejectPrivateConnection rejectPrivateConnection);

}
