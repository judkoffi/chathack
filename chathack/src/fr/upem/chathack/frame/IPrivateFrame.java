package fr.upem.chathack.frame;

import fr.upem.chathack.visitor.IPrivateFrameVisitor;

/**
 * Interface use to represent super type of all private frame exchange between clients
 */

public interface IPrivateFrame extends IFrame {
  public void accept(IPrivateFrameVisitor frameVisitor);
}

