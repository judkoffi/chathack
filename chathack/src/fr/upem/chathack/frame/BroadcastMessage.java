package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.Message;

public class BroadcastMessage implements IFrame {
  private final Message message;

  public BroadcastMessage(Message message) {
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
