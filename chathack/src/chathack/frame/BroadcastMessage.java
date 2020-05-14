package chathack.frame;

import java.nio.ByteBuffer;

public class BroadcastMessage implements IFrame {
  private String from;
  private String message;

  public BroadcastMessage(String from, String message) {
    this.from = from;
    this.message = message;
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
