package fr.upem.chathack.model;

import static fr.upem.chathack.utils.Helper.cloneByteBuffer;
import java.nio.ByteBuffer;

public class ByteLong {
  private final byte opcode;
  private final long value;
  private final ByteBuffer bb;

  public ByteLong(byte opcode, long value) {
    this.opcode = opcode;
    this.value = value;
    this.bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES);
    fillBuffer();
  }

  private void fillBuffer() {
    bb.put(opcode);
    bb.putLong(value);
  }

  public ByteBuffer toBuffer() {
    return cloneByteBuffer(bb).flip();
  }


  public byte getByte() {
    return opcode;
  }

  public long getLong() {
    return value;
  }

  @Override
  public String toString() {
    return "op: " + opcode + " value: " + value;
  }
}
