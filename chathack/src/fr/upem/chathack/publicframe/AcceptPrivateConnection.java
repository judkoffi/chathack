package fr.upem.chathack.publicframe;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

public class AcceptPrivateConnection implements IPublicFrame {
  private final LongSizedString applicant;
  private final LongSizedString receiver;
  private final InetSocketAddress targetAddress;
  private final long token;

  public AcceptPrivateConnection(LongSizedString applicant, LongSizedString receiver,
      InetSocketAddress targetAddress, long token) {
    this.applicant = applicant;
    this.receiver = receiver;
    this.targetAddress = targetAddress;
    this.token = token;
  }

  public AcceptPrivateConnection(String applicant, String receiver, InetSocketAddress targetAddress,
      long token) {
    this.applicant = new LongSizedString(applicant);
    this.receiver = new LongSizedString(receiver);
    this.targetAddress = targetAddress;
    this.token = token;
  }

  private static long ipToLong(InetSocketAddress addr) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN);
    buffer.put(new byte[] {0, 0, 0, 0});
    buffer.put(addr.getAddress().getAddress());
    buffer.flip();
    return buffer.getLong();
  }

  @Override
  public ByteBuffer toBuffer() {
    var s = Byte.BYTES + receiver.getTrameSize() + (3 * Long.BYTES) + applicant.getTrameSize();
    var bb = ByteBuffer.allocate((int) s);
    var ipInLong = ipToLong(targetAddress);
    bb.put(OpCode.SUCCEDED_PRIVATE_CLIENT_CONNECTION);
    bb.put(receiver.toBuffer());
    bb.putLong(ipInLong);
    bb.putLong((long) targetAddress.getPort());
    bb.putLong(token);
    bb.put(applicant.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public String getAppliant() {
    return applicant.getValue();
  }

  public String getReceiver() {
    return receiver.getValue();
  }

  public long getToken() {
    return token;
  }

  @Override
  public String toString() {
    return "AcceptResponse " + applicant + " " + receiver + " " + targetAddress;
  }

  public InetSocketAddress getTargetAddress() {
    return targetAddress;
  }
}
