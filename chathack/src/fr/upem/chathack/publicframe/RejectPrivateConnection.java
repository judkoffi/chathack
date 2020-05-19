package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

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
