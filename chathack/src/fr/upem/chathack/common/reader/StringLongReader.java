package fr.upem.chathack.common.reader;

import static fr.upem.chathack.utils.Helper.BUFFER_SIZE;
import static fr.upem.chathack.utils.Helper.DEFAULT_CHARSET;
import java.nio.ByteBuffer;

public class StringLongReader implements IReader<String> {
  private enum State {
    WAITING_SIZE, WAITING_OCTET_CHAINE, DONE, ERROR
  }

  private State state = State.WAITING_SIZE;
  private final LongReader longReader = new LongReader();
  private String value;
  private long size;
  private final ByteBuffer internalbb = ByteBuffer.allocate(BUFFER_SIZE);


  private ProcessStatus processSize(ByteBuffer bb) {
    var sizeStatus = longReader.process(bb);
    switch (sizeStatus) {
      case ERROR:
        return ProcessStatus.ERROR;
      case REFILL:
        return ProcessStatus.REFILL;
      case DONE:
        return ProcessStatus.DONE;
      default:
        throw new AssertionError();
    }
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_SIZE: {
        var status = processSize(bb);
        if (status != ProcessStatus.DONE)
          return status;

        size = longReader.get();
        if (size <= 0 || size > BUFFER_SIZE) {
          state = State.ERROR;
          return ProcessStatus.ERROR;
        }
        state = State.WAITING_OCTET_CHAINE;
      }
      case WAITING_OCTET_CHAINE: {
        var missing = size - internalbb.position();
        bb.flip();
        if (bb.remaining() <= missing) {
          internalbb.put(bb);
        } else {
          var oldLimit = bb.limit();
          bb.limit((int) missing);
          internalbb.put(bb);
          bb.limit(oldLimit);
        }
        bb.compact();
        if (internalbb.position() < size) {
          return ProcessStatus.REFILL;
        }
        state = State.DONE;
        internalbb.flip();
        value = DEFAULT_CHARSET.decode(internalbb).toString();
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public String get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_SIZE;
    longReader.reset();
    internalbb.clear();
    value = null;
  }
}
