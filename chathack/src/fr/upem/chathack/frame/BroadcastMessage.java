package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.Message;
import fr.upem.chathack.common.model.OpCode;
import fr.upem.chathack.frame.visitor.IPublicFrame;
import fr.upem.chathack.frame.visitor.IPublicFrameVisitor;

public class BroadcastMessage implements IPublicFrame {
  private final Message message;

  public BroadcastMessage(Message message) {
    this.message = message;
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + message.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.BROADCAST_MESSAGE);
    bb.put(message.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  @Override
  public String toString() {
    return message.toString();
  }

  public String getFromLogin() {
    return message.getFrom().getValue();
  }
}
