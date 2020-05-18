package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import java.util.Objects;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;

public class RequestPrivateConnection implements IFrame {
  private final LongSizedString fromLogin;
  private final LongSizedString targetLogin;

  public RequestPrivateConnection(LongSizedString fromLogin, LongSizedString targetLogin) {
    this.fromLogin = fromLogin;
    this.targetLogin = targetLogin;
  }

  public RequestPrivateConnection(String fromLogin, String targetLogin) {
    this.fromLogin = new LongSizedString(fromLogin);
    this.targetLogin = new LongSizedString(targetLogin);
  }

  @Override
  public ByteBuffer toBuffer() {
    var s = Byte.BYTES + fromLogin.getTrameSize() + targetLogin.getTrameSize();
    var bb = ByteBuffer.allocate((int) s);
    bb.put(OpCode.REQUEST_PRIVATE_CLIENT_CONNECTION);
    bb.put(targetLogin.toBuffer());
    bb.put(fromLogin.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public LongSizedString getFromLogin() {
    return fromLogin;
  }

  public LongSizedString getTargetLogin() {
    return targetLogin;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RequestPrivateConnection))
      return false;

    RequestPrivateConnection r = (RequestPrivateConnection) obj;
    return r.fromLogin.equals(fromLogin) && r.targetLogin.equals(targetLogin);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fromLogin, targetLogin);
  }

  @Override
  public String toString() {
    return "Request from: [" + fromLogin + "] to " + targetLogin;
  }
}
