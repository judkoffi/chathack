package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;

public class AnonymousConnection implements IFrame {
  private final LongSizedString sizedString;

  public AnonymousConnection(LongSizedString sizedString) {
    this.sizedString = sizedString;
  }

  public String getLogin() {
    return sizedString.getValue();
  }

  @Override
  public ByteBuffer toBuffer() {
    var bb = ByteBuffer.allocate(Byte.BYTES + (int) sizedString.getSize());
    bb.put(OpCode.ANONYMOUS_CLIENT_CONNECTION);
    bb.put(sizedString.toBuffer());
    return bb;
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

}
