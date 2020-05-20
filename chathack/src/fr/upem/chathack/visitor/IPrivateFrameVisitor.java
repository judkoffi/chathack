package fr.upem.chathack.visitor;

import fr.upem.chathack.privateframe.ConfirmDiscoverMessage;
import fr.upem.chathack.privateframe.DirectMessage;
import fr.upem.chathack.privateframe.DiscoverMessage;

public interface IPrivateFrameVisitor extends IFrameVisitor {

  public void visit(DirectMessage directMessage);

  public void visit(DiscoverMessage message);

  public void visit(ConfirmDiscoverMessage confirmDiscoverMessage);

}
