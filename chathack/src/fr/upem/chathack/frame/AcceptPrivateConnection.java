package fr.upem.chathack.frame;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;

public class AcceptPrivateConnection implements IFrame {
  private final LongSizedString fromLogin;
  private final LongSizedString targetLogin;
  private final InetSocketAddress targetAddress;

  public AcceptPrivateConnection(LongSizedString fromLogin, LongSizedString targetLogin,
      InetSocketAddress targetAddress) {
    this.fromLogin = fromLogin;
    this.targetLogin = targetLogin;
    this.targetAddress = targetAddress;
  }

  public AcceptPrivateConnection(String fromLogin, String targetLogin,
      InetSocketAddress targetAddress) {
    this.fromLogin = new LongSizedString(fromLogin);
    this.targetLogin = new LongSizedString(targetLogin);
    this.targetAddress = targetAddress;
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
    var s = Byte.BYTES + targetLogin.getTrameSize() + (2 * Long.BYTES) + fromLogin.getTrameSize();
    var bb = ByteBuffer.allocate((int) s);
    var ipInLong = ipToLong(targetAddress);
    bb.put(OpCode.SUCCEDED_PRIVATE_CLIENT_CONNECTION);
    bb.put(targetLogin.toBuffer());
    bb.putLong(ipInLong);
    bb.putLong((long) targetAddress.getPort());
    bb.put(fromLogin.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public String getFromLogin() {
    return fromLogin.getValue();
  }

  @Override
  public String toString() {
    return "AcceptResponse " + fromLogin + " " + targetLogin + " " + targetAddress;
  }
}
