package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.publicframe.AnonymousConnection;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.LongSizedStringReader;
/**
 * Class use to read AnonymousConnection type
 *
 */
public class AnonymousConnectionReader implements IReader<AnonymousConnection> {
  private enum State {
    WAITING_DATA, DONE, ERROR
  }

  private final LongSizedStringReader reader;
  private State state;
  private AnonymousConnection value;

  public AnonymousConnectionReader() {
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
        value = new AnonymousConnection(reader.get());
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public AnonymousConnection get() {
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
