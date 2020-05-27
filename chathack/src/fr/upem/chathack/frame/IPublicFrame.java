package fr.upem.chathack.frame;

import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * Interface use to represent super type of all public frame exchange between client and server
 */
public interface IPublicFrame extends IFrame {
  public void accept(IPublicFrameVisitor frameVisitor);
}
