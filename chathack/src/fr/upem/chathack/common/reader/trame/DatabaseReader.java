package fr.upem.chathack.common.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.LongReader;
import fr.upem.chathack.frame.DatabaseTrame;

public class DatabaseReader implements IReader<DatabaseTrame> {
  private enum State {
    WAITING_OPCODE, WAITING_RESULT, DONE, ERROR
  }

  private State state;
  private final LongReader reader;
  private DatabaseTrame value;
  private byte opcode;

  public DatabaseReader() {
    this.reader = new LongReader();
    this.state = State.WAITING_OPCODE;
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_OPCODE: {
        bb.flip();
        if (!bb.hasRemaining()) {
          bb.compact();
          return ProcessStatus.REFILL;
        }
        opcode = bb.get();
        bb.compact();
        state = State.WAITING_RESULT;
      }
      case WAITING_RESULT: {
        var status = reader.process(bb);
        if (status != ProcessStatus.DONE)
          return status;
        value = new DatabaseTrame(opcode, reader.get());
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public DatabaseTrame get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_OPCODE;
    value = null;
    opcode = 0;
    reader.reset();
  }
}


