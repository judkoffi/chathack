package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * Class use to represent a frame send by a client to server to be disconnected from server
 */
public class LogOutMessage implements IPublicFrame {
  private final LongSizedString login;

  public LogOutMessage(LongSizedString login) {
    this.login = login;
  }

  public LogOutMessage(String login) {
    this.login = new LongSizedString(login);
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + login.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.CLIENT_LOG_OUT);
    bb.put(login.toBuffer());
    return bb.flip();
  }


  /**
   * Method factory to create an instance of LogOutMessage
   * 
   * @param params: a list of constructor arguments
   * @return: a {@link LogOutMessage} object
   */
  public static LogOutMessage of(List<Box<?>> params) {
    if (params.size() != 1) {
      throw new IllegalArgumentException(params + " size is invalid");
    }
    return new LogOutMessage((LongSizedString) params.get(0).getBoxedValue());
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  /**
   * Getter of login value
   * 
   * @return: login value
   */
  public String getLogin() {
    return login.getValue();
  }
}
