package fr.upem.chathack.reader;

import java.nio.ByteBuffer;

/**
 * Class use to read int type
 *
 */
public class IntReader implements IReader<Integer> {

  private enum State {
    DONE, WAITING, ERROR
  }

  private State state = State.WAITING;
  private final ByteBuffer internalbb = ByteBuffer.allocate(Integer.BYTES); // write-mode
  private int value;

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
    value = internalbb.getInt();
    if(value <= 0 || value >= Integer.MAX_VALUE) {
    	state = State.ERROR;
    	return ProcessStatus.ERROR;
    }
    state = State.DONE;
    return ProcessStatus.DONE;
  }

  @Override
  public Integer get() {
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

