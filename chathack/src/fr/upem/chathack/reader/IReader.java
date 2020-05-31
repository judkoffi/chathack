package fr.upem.chathack.reader;

import java.nio.ByteBuffer;

/**
 * interface used to read specific type of data in a buffer
 *
 */
public interface IReader<T> {

  /**
   * 
   * return the process current's state of buffer processing
   *
   */
  public enum ProcessStatus {
    DONE, REFILL, ERROR
  }

  /**
   * Method use to process given buffer
   * 
   * @param bb: a {@link ByteBuffer} to process
   * @return ProcessStatus
   */
  public ProcessStatus process(ByteBuffer bb);

  /**
   * method to return the value read by the reader
   * 
   * @return the value read by the reader
   */
  public T get();

  /**
   * method to reset the reader
   */
  public void reset();

}
