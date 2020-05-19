package fr.upem.chathack.frame;

import fr.upem.chathack.visitor.IPublicFrameVisitor;

public interface IPublicFrame extends IFrame {
  public void accept(IPublicFrameVisitor frameVisitor);
}
