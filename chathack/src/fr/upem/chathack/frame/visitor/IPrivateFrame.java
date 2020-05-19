package fr.upem.chathack.frame.visitor;

import fr.upem.chathack.frame.IFrame;

public interface IPrivateFrame extends IFrame {
  public void accept(IPrivateFrameVisitor frameVisitor);
}

