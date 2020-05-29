package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * Class use to represent a frame send when a client connected to server with a login and password
 */
public class AuthentificatedConnection implements IPublicFrame {
  private final LongSizedString login;
  private final LongSizedString password;

  public AuthentificatedConnection(LongSizedString login, LongSizedString password) {
    this.login = login;
    this.password = password;
  }

  public AuthentificatedConnection(String login, String password) {
    this.login = new LongSizedString(login);
    this.password = new LongSizedString(password);
  }

  public static AuthentificatedConnection of(List<Box<?>> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(params + " size is invalid");
    }
    var login = (LongSizedString) params.get(0).getBoxedValue();
    var password = (LongSizedString) params.get(1).getBoxedValue();
    return new AuthentificatedConnection(login, password);
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + login.getTrameSize() + password.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.AUTHENTICATED_CLIENT_CONNECTION);
    bb.put(login.toBuffer());
    bb.put(password.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
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
    return login + " : " + password;
  }
}
