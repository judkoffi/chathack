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
   * Method use to read from socket channel
   * 
   * @throws IOException
   */
  public void doRead() throws IOException;

  /**
   * Method use to write in socket channel
   * 
   * @throws IOException
   */
  public void doWrite() throws IOException;
}
