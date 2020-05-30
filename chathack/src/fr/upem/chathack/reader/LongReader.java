package fr.upem.chathack.reader;

import java.nio.ByteBuffer;

/**
 * Class use to read Long type
 *
 */
public class LongReader implements IReader<Long> {

  private enum State {
    DONE, WAITING, ERROR
  }

  private State state = State.WAITING;
  private final ByteBuffer internalbb = ByteBuffer.allocate(Long.BYTES); // write-mode
  private long value;

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    if (state == State.DONE || state == State.ERROR) {
      throw new IllegalStateException();
    }

    bb.flip();
    try {
      if (bb.remaining() <= internalbb.remaining()) {
        internalbb.put(bb);
      } else {
        var oldLimit = bb.limit();
        bb.limit(internalbb.remaining());
        internalbb.put(bb);
        bb.limit(oldLimit);
      }
    } finally {
      bb.compact();
    }

    if (internalbb.hasRemaining()) {
      return ProcessStatus.REFILL;
    }

    internalbb.flip();
    value = internalbb.getLong();
    state = State.DONE;
    return ProcessStatus.DONE;
  }

  @Override
  public Long get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING;
    value = 0;
    internalbb.clear();
  }
}
