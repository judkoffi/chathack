package fr.upem.chathack.frame.visitor;

import fr.upem.chathack.frame.DirectMessage;
import fr.upem.chathack.frame.DiscoverMessage;

public interface IPrivateFrameVisitor extends IFrameVisitor {

  public void visit(DirectMessage directMessage);

  public void visit(DiscoverMessage message);

}
