package chathack.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import chathack.common.model.ByteLong;
import chathack.common.reader.ByteLongReader;
import chathack.common.reader.IReader;

public class DatabaseContext extends BaseContext {
  private final ByteLongReader reader = new ByteLongReader();

  public DatabaseContext(SelectionKey key) {
    super(key);
  }

  public void checkLogin(ByteBuffer bb) {
    queueMessage(bb);
  }

  @Override
  public void processIn() {
    for (;;) {
      IReader.ProcessStatus status = reader.process(bbin);
      switch (status) {
        case DONE:
          ByteLong msg = reader.get();
          reader.reset();
          System.out.println("message read : " + msg);
          break;
        case REFILL:
          return;
        case ERROR:
          silentlyClose();
          return;
      }
    }
  }

  @Override
  public void processOut() {

  }

  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return; // the selector gave a bad hint
    }
    updateInterestOps();
  }
}
