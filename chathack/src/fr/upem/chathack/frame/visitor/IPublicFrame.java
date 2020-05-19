package fr.upem.chathack.frame.visitor;

import fr.upem.chathack.frame.IFrame;

public interface IPublicFrame extends IFrame {
  public void accept(IPublicFrameVisitor frameVisitor);
}
