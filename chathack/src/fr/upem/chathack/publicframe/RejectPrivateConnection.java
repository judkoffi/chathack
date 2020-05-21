package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

public class RejectPrivateConnection implements IPublicFrame {
  private final LongSizedString appliant;
  private final LongSizedString receiver;

  public RejectPrivateConnection(LongSizedString appliant, LongSizedString receiver) {
    this.appliant = appliant;
    this.receiver = receiver;

  }

  public RejectPrivateConnection(String appliant, String receiver) {
    this.appliant = new LongSizedString(appliant);
    this.receiver = new LongSizedString(receiver);
  }

  @Override
  public ByteBuffer toBuffer() {
    var s = Byte.BYTES + receiver.getTrameSize() + appliant.getTrameSize();
    var bb = ByteBuffer.allocate((int) s);

    bb.put(OpCode.REJECTED_PRIVATE_CLIENT_CONNECTION);
    bb.put(receiver.toBuffer());
    bb.put(appliant.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public String getAppliant() {
    return appliant.getValue();
  }

  public String getReceiver() {
    return receiver.getValue();
  }
}
