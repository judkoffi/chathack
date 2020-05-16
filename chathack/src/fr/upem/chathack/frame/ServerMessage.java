package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;

public class ServerMessage implements IFrame {
  private final LongSizedString value;

  public ServerMessage(LongSizedString value) {
    this.value = value;
  }

  @Override
  public ByteBuffer toBuffer() {
    var bb = ByteBuffer.allocate(Byte.BYTES + (int) value.getSize() + Long.BYTES);
    bb.put(OpCode.SERVER_ERROR_MESSAGE);
    bb.put(value.toBuffer());
    return bb;
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  @Override
  public String toString() {
    return "ERROR: " + value.getValue();
  }
}
