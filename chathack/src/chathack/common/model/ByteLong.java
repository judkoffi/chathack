package chathack.common.model;

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
    return bb.duplicate().flip();
  }
}
