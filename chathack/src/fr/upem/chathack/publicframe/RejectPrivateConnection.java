package fr.upem.chathack.publicframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * Class use to represent a frame send through the server to notify when a client refuse a private
 * connection with an another client
 */
public class RejectPrivateConnection implements IPublicFrame {
  private final LongSizedString appliant;
  private final LongSizedString receiver;

  public RejectPrivateConnection(LongSizedString appliant, LongSizedString receiver) {
    this.appliant = appliant;
    this.receiver = receiver;
  }

  public RejectPrivateConnection(String appliant, String receiver) {
    this.appliant = new LongSizedString(appliant);
    this.receiver = new LongSizedString(receiver);
  }

  /**
   * Method factory to create an instance of RejectPrivateConnection
   * 
   * @param params: a list of constructor arguments
   * @return: a {@link RejectPrivateConnection} object
   */
  public static RejectPrivateConnection of(List<Box<?>> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(params + " size is invalid");
    }

    var receiver = (LongSizedString) params.get(0).getBoxedValue();
    var applicant = (LongSizedString) params.get(1).getBoxedValue();
    return new RejectPrivateConnection(applicant, receiver);
  }

  @Override
  public ByteBuffer toBuffer() {
    var s = Byte.BYTES + receiver.getTrameSize() + appliant.getTrameSize();
    var bb = ByteBuffer.allocate((int) s);

    bb.put(OpCode.REJECTED_PRIVATE_CLIENT_CONNECTION);
    bb.put(receiver.toBuffer());
    bb.put(appliant.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  /**
   * Getter of appliant login value
   * 
   * @return: appliant login
   */
  public String getAppliant() {
    return appliant.getValue();
  }

  /**
   * Getter of receiver login value
   * 
   * @return: receiver login
   */
  public String getReceiver() {
    return receiver.getValue();
  }

  @Override
  public String toString() {
    return "Private exchange refused by " + receiver;
  }
}
