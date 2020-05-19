package fr.upem.chathack.common.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.IntReader;
import fr.upem.chathack.common.reader.LongSizedStringReader;
import fr.upem.chathack.frame.DiscoverMessage;

public class DiscoverMessageReader implements IReader<DiscoverMessage> {

  private enum State {
    WAITING_LOGIN, WAITING_KEY_HASHCODE, DONE, ERROR
  }

  private LongSizedString login;
  private final LongSizedStringReader longStringReader;
  private final IntReader intReader;

  private State state;
  private DiscoverMessage value;

  public DiscoverMessageReader() {
    this.state = State.WAITING_LOGIN;
    this.longStringReader = new LongSizedStringReader();
    this.intReader = new IntReader();
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
        var status = intReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        value = new DiscoverMessage(login, intReader.get());
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
    intReader.reset();
    login = null;
    value = null;
  }
}
