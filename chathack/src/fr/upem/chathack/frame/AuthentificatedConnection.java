package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.BiString;
import fr.upem.chathack.common.model.OpCode;

public class AuthentificatedConnection implements IFrame {
  private final BiString message;

  public AuthentificatedConnection(BiString message) {
    this.message = message;
  }

  @Override
  public ByteBuffer toBuffer() {
    var messageBb = message.toBuffer();
    var bb = ByteBuffer.allocate(Byte.BYTES + messageBb.limit());
    bb.put(OpCode.AUTHENTICATED_CLIENT_CONNECTION);
    bb.put(messageBb);
    return bb.duplicate().flip();
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public String getLogin() {
    return message.getLogin();
  }

  @Override
  public String toString() {
    return "2 | " + message.toString();
  }

  public ByteBuffer getContentBuffer() {
    return message.toBuffer();
  }
}
