package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.frame.DatabaseTrame;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.trame.DatabaseReader;
import fr.upem.chathack.server.ServerChatHack;

/**
 * Class use to represent context between a database server and server chaton
 */
public class DatabaseContext extends BaseContext {
  private final DatabaseReader reader = new DatabaseReader();
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
          DatabaseTrame msg = reader.get();
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

  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return; // the selector gave a bad hint
    }
    updateInterestOps();
  }
}
