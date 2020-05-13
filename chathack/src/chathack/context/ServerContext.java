package chathack.context;

import java.nio.channels.SelectionKey;
import chathack.ServerChatHack;
import chathack.common.reader.IReader;
import chathack.common.reader.RequestReader;

public class ServerContext extends BaseContext {
  private final ServerChatHack server;
  private final RequestReader requestReader = new RequestReader();

  public ServerContext(ServerChatHack server, SelectionKey key) {
    super(key);
    this.server = server;
  }

  @Override
  public void processIn() {
    for (;;) {
      IReader.ProcessStatus status = requestReader.process(bbin);
      switch (status) {
        case DONE:
          var msg = requestReader.get();
          // server.broadcast(msg);
          requestReader.reset();
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
    // TODO Auto-generated method stub

  }

}
