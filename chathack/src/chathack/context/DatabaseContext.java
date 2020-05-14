package chathack.context;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import chathack.common.model.Message;
import chathack.common.reader.IReader;
import chathack.common.reader.MessageReader;

public class DatabaseContext extends BaseContext {
  private final MessageReader messageReader = new MessageReader();

  public DatabaseContext(SelectionKey key) {
    super(key);
  }

  @Override
  public void processIn() {
    for (;;) {
      IReader.ProcessStatus status = messageReader.process(bbin);
      switch (status) {
        case DONE:
          Message msg = messageReader.get();
          messageReader.reset();
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
