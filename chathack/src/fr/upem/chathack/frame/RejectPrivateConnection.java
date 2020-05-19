package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;
import fr.upem.chathack.frame.visitor.IPublicFrame;
import fr.upem.chathack.frame.visitor.IPublicFrameVisitor;

public class RejectPrivateConnection implements IPublicFrame {
  private final LongSizedString fromLogin;
  private final LongSizedString targetLogin;

  public RejectPrivateConnection(LongSizedString fromLogin, LongSizedString targetLogin) {
    this.fromLogin = fromLogin;
    this.targetLogin = targetLogin;

  }

  public RejectPrivateConnection(String fromLogin, String targetLogin) {
    this.fromLogin = new LongSizedString(fromLogin);
    this.targetLogin = new LongSizedString(targetLogin);
  }

  @Override
  public ByteBuffer toBuffer() {
    var s = Byte.BYTES + targetLogin.getTrameSize() + fromLogin.getTrameSize();
    var bb = ByteBuffer.allocate((int) s);

    bb.put(OpCode.REJECTED_PRIVATE_CLIENT_CONNECTION);
    bb.put(targetLogin.toBuffer());
    bb.put(fromLogin.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public String getFromLogin() {
    return fromLogin.getValue();

  }

}
