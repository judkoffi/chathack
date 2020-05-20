package fr.upem.chathack.privateframe;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

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
    return "recived tokne confirm from " + sender;
  }
}
