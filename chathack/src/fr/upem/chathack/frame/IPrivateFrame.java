package fr.upem.chathack.frame;

import fr.upem.chathack.visitor.IPrivateFrameVisitor;

public interface IPrivateFrame extends IFrame {
  public void accept(IPrivateFrameVisitor frameVisitor);
}

