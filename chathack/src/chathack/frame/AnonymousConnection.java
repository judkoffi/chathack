package chathack.frame;

import java.nio.ByteBuffer;
import chathack.utils.Helper;

public class AnonymousConnection implements IFrame {
  private final String login;

  public AnonymousConnection(String login) {
    this.login = login;
  }

  public String getLogin() {
    return login;
  }

  @Override
  public ByteBuffer toBuffer() {
    return Helper.DEFAULT_CHARSET.encode(login);
  }

  @Override
  public void accept(IFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

}
