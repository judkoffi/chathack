package chathack.builder;

import java.nio.ByteBuffer;

public class IntBuilder implements IBuilder<Integer> {

  @Override
  public ByteBuffer build(Integer value) {
    var bb = ByteBuffer.allocate(Integer.BYTES);
    bb.putInt(value);
    return bb;
  }
}


