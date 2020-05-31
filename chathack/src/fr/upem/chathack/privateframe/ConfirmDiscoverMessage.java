package fr.upem.chathack.privateframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

/**
 * Class use to represent a confirmation frame of token during private connection between clients
 */
public class ConfirmDiscoverMessage implements IPrivateFrame {
  private final LongSizedString sender;

  public ConfirmDiscoverMessage(String sender) {
    this.sender = new LongSizedString(sender);
  }

  public ConfirmDiscoverMessage(LongSizedString sender) {
    this.sender = sender;
  }

  /**
   * Method factory to create an instance of ConfirmDiscoverMessage
   * 
   * @param params: a list of constructor arguments
   * @return: a {@link ConfirmDiscoverMessage} object
   */
  public static ConfirmDiscoverMessage of(List<Box<?>> params) {
    if (params.size() != 1) {
      throw new IllegalArgumentException(params + " size is invalid");
    }

    var sender = (LongSizedString) params.get(0).getBoxedValue();
    return new ConfirmDiscoverMessage(sender);
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + sender.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.DISCOVER_CONFIRMATION);
    bb.put(sender.toBuffer());
    return bb.flip();
  }

  /**
   * Getter of sender login
   * 
   * @return: sender login
   */
  public String getSender() {
    return sender.getValue();
  }

  @Override
  public void accept(IPrivateFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  @Override
  public String toString() {
    return "received token confirmation from " + sender;
  }
}
