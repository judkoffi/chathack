package fr.upem.chathack.reader;

import static fr.upem.chathack.utils.Helper.BUFFER_SIZE;
import static fr.upem.chathack.utils.Helper.DEFAULT_CHARSET;
import java.nio.ByteBuffer;
import fr.upem.chathack.model.LongSizedString;

/**
 * Class used to read LongSizedString type
 *
 */
public class LongSizedStringReader implements IReader<LongSizedString> {
  private enum State {
    WAITING_SIZE, WAITING_OCTET_CHAINE, DONE, ERROR
  }

  private State state = State.WAITING_SIZE;
  private final LongReader longReader = new LongReader();
  private LongSizedString value;
  private long size;
  private final ByteBuffer internalbb = ByteBuffer.allocate(BUFFER_SIZE);


  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_SIZE: {
        var status = longReader.process(bb);
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
        value = new LongSizedString(DEFAULT_CHARSET.decode(internalbb).toString());
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public LongSizedString get() {
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
