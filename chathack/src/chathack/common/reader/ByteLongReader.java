package chathack.common.reader;

import java.nio.ByteBuffer;
import chathack.common.model.ByteLong;

public class ByteLongReader implements IReader<ByteLong> {
  private enum State {
    WAITING_OPCODE, WAITING_LONG, DONE, ERROR
  }

  private final ByteReader byteReader = new ByteReader();
  private final LongReader longReader = new LongReader();
  private ByteLong value;
  private byte opcode;
  private State state = State.WAITING_OPCODE;

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_OPCODE: {
        var opcodeStatus = byteReader.process(bb);
        if (opcodeStatus != ProcessStatus.DONE)
          return opcodeStatus;
        opcode = byteReader.get();
        state = State.WAITING_LONG;
      }
      case WAITING_LONG: {
        var status = longReader.process(bb);
        if (status != ProcessStatus.DONE)
          return status;
        state = State.DONE;
        value = new ByteLong(opcode, longReader.get());
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public ByteLong get() {
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
    byteReader.reset();
    longReader.reset();
  }
}
