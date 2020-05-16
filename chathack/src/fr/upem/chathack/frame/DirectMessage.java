package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.BiString;

public class DirectMessage implements IFrame {
  private final BiString message;
  private final String to;

  public DirectMessage(String from, String content, String to) {
    this.message = new BiString(from, content);
    this.to = to;
  }

  @Override
  public ByteBuffer toBuffer() {
    return null;
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }
}
