package fr.upem.chathack.privateframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

/**
 * Class used to represent a frame closing a private connection between clients
 *
 */
public class ClosePrivateConnectionMessage implements IPrivateFrame {
  private final LongSizedString from;

  public ClosePrivateConnectionMessage(String login) {
    this.from = new LongSizedString(login);
  }

  public ClosePrivateConnectionMessage(LongSizedString from) {
    this.from = from;
  }

  /**
   * Method factory to create an instance of ClosePrivateConnectionMessage
   * 
   * @param params: a list of constructor arguments
   * @return: a {@link ClosePrivateConnectionMessage} object
   */
  public static ClosePrivateConnectionMessage of(List<Box<?>> params) {
    if (params.size() != 1) {
      throw new IllegalArgumentException(params + " size is invalid");
    }

    var from = (LongSizedString) params.get(0).getBoxedValue();
    return new ClosePrivateConnectionMessage(from);
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + from.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.PRIVATE_CONNECTION_CLOSE);
    bb.put(from.toBuffer());
    return bb.flip();
  }

  /**
   * Getter of from value
   * 
   * @return: login of from client
   */
  public String getFrom() {
    return from.getValue();
  }

  @Override
  public void accept(IPrivateFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

}
