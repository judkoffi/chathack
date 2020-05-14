package chathack.frame;

import java.nio.ByteBuffer;

public class DirectMessage implements IFrame {

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
