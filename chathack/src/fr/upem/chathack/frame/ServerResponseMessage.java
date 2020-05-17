package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;

/**
 * ----------------------------------------------------------------- --<br>
 * | Opcode (byte) | errorOrNot (-1 or 1) (byte) | length (long) | msg (string)|<br>
 * --------------------------------------------------------------------<br>
 */
public class ServerResponseMessage implements IFrame {
  private final LongSizedString value;
  private final boolean errorMessage;

  public ServerResponseMessage(LongSizedString value, boolean errorMessage) {
    this.value = value;
    this.errorMessage = errorMessage;
  }

  public ServerResponseMessage(String value, boolean errorMessage) {
    this.value = new LongSizedString(value);
    this.errorMessage = errorMessage;
  }

  @Override
  public ByteBuffer toBuffer() {
    byte typeOpcode = errorMessage //
        ? OpCode.SERVER_ERROR_RESPONSE_TYPE
        : OpCode.SERVER_NOT_ERROR_RESPONSE_TYPE;

    var bb = ByteBuffer.allocate(Byte.BYTES + Byte.BYTES + (int) value.getSize() + Long.BYTES);
    bb.put(OpCode.SERVER_RESPONSE_MESSAGE);
    bb.put(typeOpcode);
    bb.put(value.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public boolean isErrorMessage() {
    return errorMessage;
  }

  @Override
  public String toString() {
    return errorMessage ? "ERROR: " + value.getValue() : value.getValue();
  }
}
