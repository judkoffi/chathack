package fr.upem.chathack.privateframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.Message;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

/**
 * Class use to represent a private message frame send between two clients
 */
public class DirectMessage implements IPrivateFrame {
  private final Message message;

  public DirectMessage(String fromLogin, String message) {
    this.message = new Message(fromLogin, message);
  }

  public DirectMessage(Message content) {
    this.message = content;
  }

  public static DirectMessage of(List<Box<?>> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(params + " size is invalid");
    }

    var from = (LongSizedString) params.get(0).getBoxedValue();
    var content = (LongSizedString) params.get(1).getBoxedValue();
    var message = new Message(from, content);
    return new DirectMessage(message);
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + message.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.DIRECT_MESSAGE);
    bb.put(message.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPrivateFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  @Override
  public String toString() {
    return "" + message;
  }
}
