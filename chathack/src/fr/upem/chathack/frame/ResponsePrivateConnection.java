package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;

public class ResponsePrivateConnection implements IFrame {
  private final LongSizedString fromLogin;
  private final LongSizedString targetLogin;
  private final boolean accepted;

  public ResponsePrivateConnection(LongSizedString fromLogin, LongSizedString targetLogin,
      boolean accepted) {
    this.fromLogin = fromLogin;
    this.targetLogin = targetLogin;
    this.accepted = accepted;
  }

  @Override
  public ByteBuffer toBuffer() {
    return null;
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

}
