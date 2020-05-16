package fr.upem.chathack.frame;

import java.nio.ByteBuffer;

public class DatabaseTrame {
  private final byte opcode;
  private final long result;
  private final ByteBuffer bb;

  public DatabaseTrame(byte opcode, long result) {
    this.opcode = opcode;
    this.result = result;
    this.bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);
    fillBuffer();
  }

  private void fillBuffer() {
    bb.put(opcode);
    bb.putLong(result);
  }

  public ByteBuffer toBuffer() {
    return bb.duplicate().flip();
  }

  public long getResult() {
    return result;
  }

  public byte getOpCode() {
    return opcode;
  }

  @Override
  public String toString() {
    return "DBTrame op: " + opcode + " ==> result " + result;
  }
}
