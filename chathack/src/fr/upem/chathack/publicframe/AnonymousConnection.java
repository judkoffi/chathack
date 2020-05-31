package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * Class use to represent a frame send when a client connected to server with only a login (no
 * password)
 */
public class AnonymousConnection implements IPublicFrame {
  private final LongSizedString sizedString;

  public AnonymousConnection(LongSizedString sizedString) {
    this.sizedString = sizedString;
  }

  /**
   * Method factory to create an instance of AnonymousConnection
   * 
   * @param params: a list of constructor arguments
   * @return: a {@link AnonymousConnection} object
   */
  public static AnonymousConnection of(List<Box<?>> params) {
    if (params.size() != 1) {
      throw new IllegalArgumentException(params + " size is invalid");
    }
    return new AnonymousConnection((LongSizedString) params.get(0).getBoxedValue());
  }

  public AnonymousConnection(String login) {
    this.sizedString = new LongSizedString(login);
  }

  /**
   * Getter of login value
   * 
   * @return: login value
   */
  public String getLogin() {
    return sizedString.getValue();
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + sizedString.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.ANONYMOUS_CLIENT_CONNECTION);
    bb.put(sizedString.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }
}
