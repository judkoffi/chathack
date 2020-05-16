package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;

public class AuthentificatedConnection implements IFrame {
  private final LongSizedString login;
  private final LongSizedString password;

  public AuthentificatedConnection(LongSizedString login, LongSizedString password) {
    this.login = login;
    this.password = password;
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + (int) login.getSize() + (int) password.getSize();
    var bb = ByteBuffer.allocate(size);
    bb.put(OpCode.AUTHENTICATED_CLIENT_CONNECTION);
    bb.put(login.toBuffer());
    bb.put(password.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public LongSizedString getLogin() {
    return login;
  }

  public LongSizedString getPassword() {
    return password;
  }

  @Override
  public String toString() {
    return "2 | " + login + " : " + password;
  }
}
