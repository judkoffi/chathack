package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.LongSizedStringReader;

public class ServerMessageReader implements IReader<ServerMessage> {
  private enum State {
    WAITING_DATA, DONE, ERROR
  }

  private final LongSizedStringReader reader;
  private State state;
  private ServerMessage value;

  public ServerMessageReader() {
    this.reader = new LongSizedStringReader();
    this.state = State.WAITING_DATA;
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_DATA: {
        var status = reader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        value = new ServerMessage(reader.get());
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public ServerMessage get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_DATA;
    reader.reset();
    value = null;
  }

}
