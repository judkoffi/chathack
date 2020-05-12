package chathack.writer;

import java.nio.ByteBuffer;

public class IntWriter implements IWriter<Integer> {

  @Override
  public ByteBuffer build(Integer value) {
    var bb = ByteBuffer.allocate(Integer.BYTES);
    bb.putInt(value);
    return bb;
  }
}


