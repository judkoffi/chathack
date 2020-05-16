package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.ServerChatHack;
import fr.upem.chathack.common.model.ByteLong;
import fr.upem.chathack.common.reader.ByteLongReader;
import fr.upem.chathack.common.reader.IReader;

public class DatabaseContext extends BaseContext {
  private final ByteLongReader reader = new ByteLongReader();
  private final ServerChatHack server;

  public DatabaseContext(SelectionKey key, ServerChatHack server) {
    super(key);
    this.server = server;
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
          server.responseCheckLogin(msg);
          reader.reset();
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
    while (!queue.isEmpty()) {
      var bb = queue.peek();
      if (bbout.remaining() < bb.remaining())
        return;

      queue.remove();
      bbout.put(bb);
    }
  }

  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return; // the selector gave a bad hint
    }
    updateInterestOps();
  }
}
