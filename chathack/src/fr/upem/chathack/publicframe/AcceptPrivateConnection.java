package fr.upem.chathack.publicframe;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * Class use to represent a frame send through the server to notify when a client accept a private
 * connection with an another client
 */
public class AcceptPrivateConnection implements IPublicFrame {
  private final LongSizedString applicant;
  private final LongSizedString receiver;
  private final InetSocketAddress targetAddress;
  private final long token;

  public AcceptPrivateConnection(LongSizedString applicant, LongSizedString receiver,
      InetSocketAddress targetAddress, long token) {
    this.applicant = applicant;
    this.receiver = receiver;
    this.targetAddress = targetAddress;
    this.token = token;
  }

  public AcceptPrivateConnection(String applicant, String receiver, InetSocketAddress targetAddress,
      long token) {
    this.applicant = new LongSizedString(applicant);
    this.receiver = new LongSizedString(receiver);
    this.targetAddress = targetAddress;
    this.token = token;
  }

  /**
   * Method factory to create an instance of AcceptPrivateConnection
   * 
   * @param params: a list of constructor arguments
   * @return: a {@link AcceptPrivateConnection} object
   */
  public static AcceptPrivateConnection of(List<Box<?>> params) {
    if (params.size() != 4) {
      throw new IllegalArgumentException(params + " size is invalid");
    }

    var applicant = (LongSizedString) params.get(3).getBoxedValue();
    var receiver = (LongSizedString) params.get(0).getBoxedValue();
    var targetAddress = (InetSocketAddress) params.get(1).getBoxedValue();
    var token = (Long) params.get(2).getBoxedValue();
    return new AcceptPrivateConnection(applicant, receiver, targetAddress, token);
  }

  /**
   * Getter of applicant login
   * 
   * @return: applicant login value
   */
  public String getAppliant() {
    return applicant.getValue();
  }

  /**
   * Getter of receiver login
   * 
   * @return: receiver login value
   */
  public String getReceiver() {
    return receiver.getValue();
  }

  /**
   * Getter of token value
   * 
   * @return: token value
   */
  public long getToken() {
    return token;
  }

  @Override
  public ByteBuffer toBuffer() {
    var bytes = targetAddress.getAddress().getAddress();
    var bytesSize = Byte.BYTES * bytes.length;
    var s = Byte.BYTES + receiver.getTrameSize() + (2 * Integer.BYTES) + bytesSize + Long.BYTES
        + applicant.getTrameSize();
    var bb = ByteBuffer.allocate((int) s);

    bb.put(OpCode.SUCCEDED_PRIVATE_CLIENT_CONNECTION);
    bb.put(receiver.toBuffer());
    bb.putInt(bytes.length);// ip byte size
    bb.put(bytes);// ip addr bytes
    bb.putInt(targetAddress.getPort()); // port
    bb.putLong(token);// token
    bb.put(applicant.toBuffer());
    return bb.flip();
  }

  @Override
  public void accept(IPublicFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  @Override
  public String toString() {
    return "AcceptResponse " + applicant + " " + receiver + " " + targetAddress;
  }

  public InetSocketAddress getTargetAddress() {
    return targetAddress;
  }
}
