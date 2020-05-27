package fr.upem.chathack.privateframe;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

/**
 * Class use to represent a first frame send when private connection was established to exchange
 * token
 */
public class DiscoverMessage implements IPrivateFrame {
  private final LongSizedString sizedString;
  private final long token;

  public DiscoverMessage(LongSizedString sizedString, long token) {
    this.sizedString = sizedString;
    this.token = token;
  }

  public DiscoverMessage(String login, long token) {
    this.sizedString = new LongSizedString(login);
    this.token = token;
  }

  public String getLogin() {
    return sizedString.getValue();
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + Long.BYTES + sizedString.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.DISCOVER_MESSAGE);
    bb.put(sizedString.toBuffer());
    bb.putLong(token);
    return bb.flip();
  }

  @Override
  public void accept(IPrivateFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  @Override
  public String toString() {
    return "discover login " + getLogin() + " key: " + token;
  }

  public long getToken() {
    return token;
  }
}
