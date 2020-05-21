package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * ----------------------------------------------------------------- --<br>
 * | Opcode (byte) | errorOrNot (-1 or 1) (byte) | length (long) | msg (string)|<br>
 * --------------------------------------------------------------------<br>
 */
public class ServerResponseMessage implements IPublicFrame {
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

    var size = Byte.BYTES + Byte.BYTES + value.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.SERVER_RESPONSE_MESSAGE);
    bb.put(typeOpcode);
    bb.put(value.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
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