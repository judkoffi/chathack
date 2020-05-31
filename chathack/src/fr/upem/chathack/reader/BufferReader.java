package fr.upem.chathack.reader;

import static fr.upem.chathack.utils.Helper.LIMIT_FILE_CONTENT_SIZE;
import java.nio.ByteBuffer;
/**
 * Class used to read ByteBuffer type
 *
 */
public class BufferReader implements IReader<ByteBuffer> {
  private enum State {
    WAITING_SIZE, WAITING_CONTENT, DONE, ERROR
  }

  private IntReader intReader = new IntReader();
  private int size;
  private State state = State.WAITING_SIZE;
  private ByteBuffer value;
  private ByteBuffer internalbb = ByteBuffer.allocate(LIMIT_FILE_CONTENT_SIZE);


  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_SIZE: {
        var status = intReader.process(bb);
        if (status != ProcessStatus.DONE)
          return status;

        size = intReader.get();
        if (size <= 0 || size > LIMIT_FILE_CONTENT_SIZE) {
          state = State.ERROR;
          return ProcessStatus.ERROR;
        }

        state = State.WAITING_CONTENT;
      }
      case WAITING_CONTENT: {
        var missing = size - internalbb.position();
        bb.flip();
        if (bb.remaining() <= missing) {
          internalbb.put(bb);
        } else {
          var oldLimit = bb.limit();
          bb.limit(missing);
          internalbb.put(bb);
          bb.limit(oldLimit);
        }
        bb.compact();
        if (internalbb.position() < size) {
          return ProcessStatus.REFILL;
        }
        state = State.DONE;
        internalbb.flip();
        value = ByteBuffer.allocate(size);
        value.put(internalbb);
        value.flip();
        return ProcessStatus.DONE;
      }

      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public ByteBuffer get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_SIZE;
    intReader.reset();
    size = 0;
    value = null;
    internalbb.clear();
  }
}
