package chathack.frame;

import java.nio.ByteBuffer;
import chathack.common.model.Message;

public class AuthentificatedConnection implements IFrame {
  private final Message message;

  public AuthentificatedConnection(Message message) {
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
