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
  private final LongSizedString destinator;
  private final LongSizedString sender;

  public ConfirmDiscoverMessage(String destinator, String sender) {
    this.destinator = new LongSizedString(destinator);
    this.sender = new LongSizedString(sender);
  }

  public ConfirmDiscoverMessage(LongSizedString destinator, LongSizedString sender) {
    this.destinator = destinator;
    this.sender = sender;
  }

  public static ConfirmDiscoverMessage of(List<Box<?>> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(params + " size is invalid");
    }

    var destinator = (LongSizedString) params.get(0).getBoxedValue();
    var sender = (LongSizedString) params.get(1).getBoxedValue();
    return new ConfirmDiscoverMessage(destinator, sender);
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + destinator.getTrameSize() + sender.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.DISCOVER_CONFIRMATION);
    bb.put(destinator.toBuffer());
    bb.put(sender.toBuffer());
    return bb.flip();
  }

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
