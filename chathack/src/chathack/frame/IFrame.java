package chathack.frame;

import java.nio.ByteBuffer;

public interface IFrame {
  public ByteBuffer toBuffer();

  public void accept(IFrameVisitor frameVisitor);
}
