package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

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
