package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface use to have mandatory methods of a context
 */
public interface IContext {
  /**
   * 
   * Method use to process buffer read from socket channel
   */
  public void processIn();

  /**
   * Method use to process buffer which write in socket channel
   */
  public void processOut();

  /**
   * Method use to add buffer in context output buffer
   * 
   * @param bb: buffer to be send
   */
  public void queueMessage(ByteBuffer bb);

  /**
   * Method use to update selector interest key
   */
  public void updateInterestOps();

  /**
   * Method use to close without throw an exception a client
   */
  public void silentlyClose();

  /**
   * Performs the read action on sc
   *
   * The convention is that both buffers are in write-mode before the call to doRead and after the
   * call
   *
   * @throws IOException
   */
  public void doRead() throws IOException;

  /**
   * Performs the write action on sc
   *
   * The convention is that both buffers are in write-mode before the call to doWrite and after the
   * call
   *
   * @throws IOException
   */
  public void doWrite() throws IOException;
}
