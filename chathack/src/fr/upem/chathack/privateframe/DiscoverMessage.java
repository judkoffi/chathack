package fr.upem.chathack.privateframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

/**
 * Class use to represent a first frame send when private connection was established to exchange
 * token
 */
public class DiscoverMessage implements IPrivateFrame {
  private final LongSizedString login;
  private final long token;

  public DiscoverMessage(LongSizedString login, long token) {
    this.login = login;
    this.token = token;
  }

  public DiscoverMessage(String login, long token) {
    this.login = new LongSizedString(login);
    this.token = token;
  }

  public static DiscoverMessage of(List<Box<?>> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(params + " size is invalid");
    }

    var destinator = (LongSizedString) params.get(0).getBoxedValue();
    var token = (Long) params.get(1).getBoxedValue();
    return new DiscoverMessage(destinator, token);
  }

  public String getLogin() {
    return login.getValue();
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + Long.BYTES + login.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.DISCOVER_MESSAGE);
    bb.put(login.toBuffer());
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
