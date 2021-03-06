package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.Message;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * Class use to represent a frame contains a public message to be send to all connected client
 */
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

  /**
   * Getter of from login value
   * 
   * @return: from login value
   */
  public String getFromLogin() {
    return message.getFrom().getValue();
  }

  @Override
  public String toString() {
    return message.toString();
  }
}
