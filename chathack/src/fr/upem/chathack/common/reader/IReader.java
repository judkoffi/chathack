package fr.upem.chathack.common.reader;

import java.nio.ByteBuffer;

public interface IReader<T> {

  public enum ProcessStatus {
    DONE, REFILL, ERROR
  }

  public ProcessStatus process(ByteBuffer bb);

  public T get();

  public void reset();

}
