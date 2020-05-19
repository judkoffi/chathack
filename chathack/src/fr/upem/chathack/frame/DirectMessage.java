package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.Message;
import fr.upem.chathack.common.model.OpCode;

public class DirectMessage implements IFrame {
  private final LongSizedString destinator;
  private final Message message;

  public DirectMessage(String fromLogin, String targetLogin, String message) {
    this.destinator = new LongSizedString(targetLogin);
    this.message = new Message(fromLogin, message);
  }

  public DirectMessage(LongSizedString target, Message content) {
    this.destinator = target;
    this.message = content;
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + message.getTrameSize() + destinator.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.DIRECT_MESSAGE);
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
