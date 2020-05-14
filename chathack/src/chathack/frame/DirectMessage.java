package chathack.frame;

import java.nio.ByteBuffer;
import chathack.common.model.Message;

public class DirectMessage implements IFrame {
  private final Message message;
  private final String to;

  public DirectMessage(String from, String content, String to) {
    this.message = new Message(from, content);
    this.to = to;
  }

  @Override
  public ByteBuffer toBuffer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }
}
