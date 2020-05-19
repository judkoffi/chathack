package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;
import fr.upem.chathack.frame.visitor.IPublicFrame;
import fr.upem.chathack.frame.visitor.IPublicFrameVisitor;

public class AnonymousConnection implements IPublicFrame {
  private final LongSizedString sizedString;

  public AnonymousConnection(LongSizedString sizedString) {
    this.sizedString = sizedString;
  }

  public AnonymousConnection(String login) {
    this.sizedString = new LongSizedString(login);
  }

  public String getLogin() {
    return sizedString.getValue();
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + sizedString.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.ANONYMOUS_CLIENT_CONNECTION);
    bb.put(sizedString.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }
}
