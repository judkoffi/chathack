package chathack.writer;

import java.nio.ByteBuffer;

public interface IWriter<E> {
  public ByteBuffer build(E value);
}
