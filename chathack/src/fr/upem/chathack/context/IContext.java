package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface IContext {
  public void processIn();

  public void processOut();

  public void queueMessage(ByteBuffer bb);

  public void updateInterestOps();

  public void silentlyClose();

  public void doRead() throws IOException;

  public void doWrite() throws IOException;
}
