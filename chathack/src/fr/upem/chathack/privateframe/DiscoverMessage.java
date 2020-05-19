package fr.upem.chathack.privateframe;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

public class DiscoverMessage implements IPrivateFrame {
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
  public void accept(IPrivateFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  @Override
  public String toString() {
    return "discover login " + getLogin() + " key: " + keyHashCode;
  }
}
