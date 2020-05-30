package fr.upem.chathack.dbframe;

import java.nio.ByteBuffer;

import fr.upem.chathack.frame.IDatabaseFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.utils.OpCode;

public class CheckCredentialMessage implements IDatabaseFrame {
  private final LongSizedString login;
  private final LongSizedString password;
  private final long id;

  public CheckCredentialMessage(String login, String password, long id) {
    this.login = new LongSizedString(login);
    this.password = new LongSizedString(password);
    this.id = id;
  }

  public CheckCredentialMessage(LongSizedString login, LongSizedString password, long id) {
    this.login = login;
    this.password = password;
    this.id = id;
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + Long.BYTES + (2 * Integer.BYTES) + login.getContentSize()
        + password.getContentSize();
    var requestBuffer = ByteBuffer.allocate((int) size);
    requestBuffer.put(OpCode.ASK_CREDENTIAL);
    requestBuffer.putLong(id);
    requestBuffer.put(login.toIntBuffer());
    requestBuffer.put(password.toIntBuffer());
    return requestBuffer.flip();
  }
}
