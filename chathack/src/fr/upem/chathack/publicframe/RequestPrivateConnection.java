package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import java.util.Objects;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

public class RequestPrivateConnection implements IPublicFrame {
  private final LongSizedString appliant;
  private final LongSizedString receiver;

  public RequestPrivateConnection(LongSizedString appliant, LongSizedString receiver) {
    this.appliant = appliant;
    this.receiver = receiver;
  }

  public RequestPrivateConnection(String appliant, String receiver) {
    this.appliant = new LongSizedString(appliant);
    this.receiver = new LongSizedString(receiver);
  }

  @Override
  public ByteBuffer toBuffer() {
    var s = Byte.BYTES + appliant.getTrameSize() + receiver.getTrameSize();
    var bb = ByteBuffer.allocate((int) s);
    bb.put(OpCode.REQUEST_PRIVATE_CLIENT_CONNECTION);
    bb.put(receiver.toBuffer());
    bb.put(appliant.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public LongSizedString getAppliant() {
    return appliant;
  }

  public LongSizedString getReceiver() {
    return receiver;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RequestPrivateConnection))
      return false;

    RequestPrivateConnection r = (RequestPrivateConnection) obj;
    return r.appliant.equals(appliant) && r.receiver.equals(receiver);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appliant, receiver);
  }

  @Override
  public String toString() {
    return "Request from: [" + appliant + "] to " + receiver;
  }
}
