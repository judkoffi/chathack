package chathack.frame;

import java.nio.ByteBuffer;
import chathack.common.model.Message;

public class BroadcastMessage implements IFrame {
  private final Message message;

  public BroadcastMessage(String from, String content) {
    this.message = new Message(from, content);
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
