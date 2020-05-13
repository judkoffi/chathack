package chathack.context;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class DatabaseContext extends BaseContext {

  public DatabaseContext(SelectionKey key) {
    super(key);
  }

  @Override
  public void processIn() {

  }

  @Override
  public void processOut() {

  }

  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return; // the selector gave a bad hint
    }
    System.out.println("klnkn");
    updateInterestOps();
  }
}
