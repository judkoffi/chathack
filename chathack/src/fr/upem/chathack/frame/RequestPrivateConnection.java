package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
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
    var s = Byte.BYTES + 2 * Long.BYTES + (int) fromLogin.getSize() + (int) targetLogin.getSize();
    var bb = ByteBuffer.allocate(s);
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
  public String toString() {
    return "4 | " + fromLogin + " : " + targetLogin;
  }
}
