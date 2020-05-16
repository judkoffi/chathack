package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.common.model.ByteLong;
import fr.upem.chathack.common.reader.ByteLongReader;
import fr.upem.chathack.common.reader.IReader;

public class DatabaseContext extends BaseContext {
  private final ByteLongReader reader = new ByteLongReader();

  public DatabaseContext(SelectionKey key) {
    super(key);
  }

  public void checkLogin(ByteBuffer bb) {
    System.out.println(bb);
    queueMessage(bb);
  }

  @Override
  public void processIn() {
    for (;;) {
      System.out.println("bb: "+bbin.hasRemaining());
      IReader.ProcessStatus status = reader.process(bbin);
      switch (status) {
        case DONE:
          ByteLong msg = reader.get();
          System.out.println("message read : " + msg);
          reader.reset();
          break;
        case REFILL:
          System.out.println("klsncqlkn");
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
