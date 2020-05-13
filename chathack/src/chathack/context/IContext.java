package chathack.context;

import java.io.IOException;

public interface IContext {
  public void processIn();

  public void processOut();

  public void updateInterestOps();

  public void silentlyClose();

  public void doRead() throws IOException;

  public void doWrite() throws IOException;
}
