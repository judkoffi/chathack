package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.Message;
import fr.upem.chathack.common.model.OpCode;

public class DirectMessage implements IFrame {
  private final Message message;
  private final LongSizedString destinator;

  public DirectMessage(String fromLogin, String targetLogin, String message) {
    this.message = new Message(fromLogin, message);
    this.destinator = new LongSizedString(targetLogin);
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + message.getTrameSize() + destinator.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.PRIVATE_MESSAGE);
    bb.put(destinator.toBuffer());
    bb.put(message.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public String getDestinator() {
    return destinator.getValue();
  }
}
