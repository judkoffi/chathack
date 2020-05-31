package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.dbframe.DatabaseResponseMessage;
import fr.upem.chathack.reader.ByteReader;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.LongReader;
import fr.upem.chathack.reader.builder.ReaderBuilder;
import fr.upem.chathack.server.ServerChatHack;

/**
 * Class use to represent context between a database server and server ChatHack
 */
public class DatabaseContext extends BaseContext {
  private final ByteReader byteReader = new ByteReader();
  private final LongReader longReader = new LongReader();

  private final IReader<DatabaseResponseMessage> reader = ReaderBuilder
    .<DatabaseResponseMessage>create()
    .addSubReader(byteReader)
    .addSubReader(longReader)
    .addConstructor(DatabaseResponseMessage::of)
    .build();

  private final ServerChatHack server;

  public DatabaseContext(SelectionKey key, ServerChatHack server) {
    super(key);
    this.server = server;
  }

  @Override
  public void processIn() {
    for (;;) {
      IReader.ProcessStatus status = reader.process(bbin);
      switch (status) {
        case DONE:
          DatabaseResponseMessage msg = reader.get();
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

  /**
   * Method use to do connection when selector notify with OP_CONNECT
   * 
   * @throws IOException
   */
  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return; // the selector gave a bad hint
    }
    updateInterestOps();
  }
}
