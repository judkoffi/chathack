package fr.upem.chathack.frame;

import fr.upem.chathack.visitor.IPrivateFrameVisitor;

/**
 * Interface use to represent super type of all private frame exchange between clients
 */

public interface IPrivateFrame extends IFrame {
  /**
   * Method use accept a {@link IPrivateFrameVisitor} visitor
   * 
   * @param frameVisitor: a private frame visitor
   */
  public void accept(IPrivateFrameVisitor frameVisitor);
}

