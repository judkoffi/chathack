package fr.upem.chathack.dbframe;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IDatabaseFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.utils.OpCode;
/**
 * Class used to check if a login is followed by a message for connection
 *
 */
public class CheckLoginMessage implements IDatabaseFrame {
  private final LongSizedString login;
  private final long id;

  public CheckLoginMessage(String login, long id) {
    this.login = new LongSizedString(login);
    this.id = id;
  }

  public CheckLoginMessage(LongSizedString login, long id) {
    this.login = login;
    this.id = id;
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + Long.BYTES + Integer.BYTES + login.getContentSize();
    var requestBuffer = ByteBuffer.allocate((int) size);
    requestBuffer.put(OpCode.ASK_LOGIN_PRESENT);
    requestBuffer.putLong(id);
    requestBuffer.put(login.toIntBuffer());
    return requestBuffer.flip();
  }
}
