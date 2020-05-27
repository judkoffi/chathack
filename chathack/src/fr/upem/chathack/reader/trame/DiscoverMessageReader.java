package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.privateframe.DiscoverMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.LongReader;
import fr.upem.chathack.reader.LongSizedStringReader;
/**
 * Class use to read DiscoverMessage type
 *
 */
public class DiscoverMessageReader implements IReader<DiscoverMessage> {

  private enum State {
    WAITING_LOGIN, WAITING_KEY_HASHCODE, DONE, ERROR
  }

  private LongSizedString login;
  private final LongSizedStringReader longStringReader;
  private final LongReader longReader;

  private State state;
  private DiscoverMessage value;

  public DiscoverMessageReader() {
    this.state = State.WAITING_LOGIN;
    this.longStringReader = new LongSizedStringReader();
    this.longReader = new LongReader();
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_LOGIN: {
        var status = longStringReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        login = longStringReader.get();
        state = State.WAITING_KEY_HASHCODE;
      }
      case WAITING_KEY_HASHCODE: {
        var status = longReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        value = new DiscoverMessage(login, longReader.get());
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public DiscoverMessage get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_LOGIN;
    longStringReader.reset();
    longReader.reset();
    login = null;
    value = null;
  }
}
