package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;

public class DiscoverMessage implements IFrame {
  private final LongSizedString sizedString;
  private final int keyHashCode;


  public DiscoverMessage(LongSizedString sizedString, int keyHashCode) {
    this.sizedString = sizedString;
    this.keyHashCode = keyHashCode;
  }

  public DiscoverMessage(String login, int keyHashCode) {
    this.sizedString = new LongSizedString(login);
    this.keyHashCode = keyHashCode;
  }

  public String getLogin() {
    return sizedString.getValue();
  }

  public int getKeyHashCode() {
    return keyHashCode;
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + Integer.BYTES + sizedString.getTrameSize();
    System.out.println("keyhash code: " + keyHashCode);
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.DISCOVER_MESSAGE);
    bb.put(sizedString.toBuffer());
    bb.putInt(keyHashCode);
    return bb.flip();
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  @Override
  public String toString() {
    return "discover login " + getLogin() + " key: " + keyHashCode;
  }
}
