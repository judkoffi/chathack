package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.BiString;

public class BroadcastMessage implements IFrame {
  private final BiString message;

  public BroadcastMessage(BiString message) {
    this.message = message;
  }

  @Override
  public ByteBuffer toBuffer() {
    return message.toBuffer();
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

}
