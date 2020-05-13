package chathack.builder;

import java.nio.ByteBuffer;

public interface IBuilder<E> {
  public ByteBuffer build(E value);
}
