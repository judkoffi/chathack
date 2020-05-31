package fr.upem.chathack.dbframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.reader.builder.Box;

/**
 * Class use to represent frame exchange between server and database server
 */
public class DatabaseResponseMessage {
  private final byte opcode;
  private final long result;
  private final ByteBuffer bb;

  public DatabaseResponseMessage(byte opcode, long result) {
    this.opcode = opcode;
    this.result = result;
    this.bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);
    fillBuffer();
  }

  /**
   * Method factory to create an instance of DatabaseResponseMessage
   * 
   * @param params: a list of constructor arguments
   * @return: a {@link DatabaseResponseMessage}
   */
  public static DatabaseResponseMessage of(List<Box<?>> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(params + "size is not valid");
    }
    var opcode = (Byte) params.get(0).getBoxedValue();
    var result = (Long) params.get(1).getBoxedValue();
    return new DatabaseResponseMessage(opcode, result);
  }

  private void fillBuffer() {
    bb.put(opcode);
    bb.putLong(result);
  }

  public ByteBuffer toBuffer() {
    return bb.duplicate().flip();
  }

  /**
   * 
   * field result getter
   * 
   * @return: current frame result value
   */
  public long getResult() {
    return result;
  }

  /**
   * field opcode getter
   * 
   * @return: current frame opcode value
   */
  public byte getOpCode() {
    return opcode;
  }

  @Override
  public String toString() {
    return "DBTrame op: " + opcode + " ==> result " + result;
  }
}
