package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * ----------------------------------------------------------------- --<br>
 * | Opcode (byte) | errorOrNot (-1 or 1) (byte) | length (long) | msg (string)|<br>
 * --------------------------------------------------------------------<br>
 */
/**
 * Class use to represent a frame send by the server to send message to client (notification, error)
 */
public class ServerResponseMessage implements IPublicFrame {
  private final boolean errorMessage;
  private final LongSizedString value;

  public ServerResponseMessage(LongSizedString value, boolean errorMessage) {
    this.value = value;
    this.errorMessage = errorMessage;
  }

  public ServerResponseMessage(String value, boolean errorMessage) {
    this.value = new LongSizedString(value);
    this.errorMessage = errorMessage;
  }

  public static ServerResponseMessage of(List<Box<?>> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(params + " size is invalid");
    }

    boolean isError = (Byte) params.get(0).getBoxedValue() == OpCode.SERVER_ERROR_RESPONSE_TYPE;
    var value = (LongSizedString) params.get(1).getBoxedValue();
    return new ServerResponseMessage(value, isError);
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
