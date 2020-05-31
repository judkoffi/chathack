package fr.upem.chathack.reader;

import java.nio.ByteBuffer;
/**
 * Class used to read Byte type
 *
 */
public class ByteReader implements IReader<Byte> {
  private enum State {
    DONE, WAITING, ERROR
  }

  private State state = State.WAITING;
  private final ByteBuffer internalbb = ByteBuffer.allocate(Byte.BYTES); // write-mode
  private byte value;

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

    state = State.DONE;
    internalbb.flip();
    value = internalbb.get();
    return ProcessStatus.DONE;
  }

  @Override
  public Byte get() {
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
