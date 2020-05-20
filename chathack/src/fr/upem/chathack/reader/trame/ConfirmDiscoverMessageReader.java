package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.privateframe.ConfirmDiscoverMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.LongSizedStringReader;

public class ConfirmDiscoverMessageReader implements IReader<ConfirmDiscoverMessage> {
  private enum State {
    WAITING_RECEIVER_LOGIN, WAITING_SENDER_LOGIN, DONE, ERROR
  }

  private LongSizedString receiverLogin;
  private final LongSizedStringReader longStringReader;
  private State state;
  private ConfirmDiscoverMessage value;

  public ConfirmDiscoverMessageReader() {
    this.state = State.WAITING_RECEIVER_LOGIN;
    this.longStringReader = new LongSizedStringReader();
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_RECEIVER_LOGIN: {
        var status = longStringReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        receiverLogin = longStringReader.get();
        longStringReader.reset();
        state = State.WAITING_SENDER_LOGIN;
      }
      case WAITING_SENDER_LOGIN: {
        var status = longStringReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        value = new ConfirmDiscoverMessage(receiverLogin, longStringReader.get());
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public ConfirmDiscoverMessage get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_RECEIVER_LOGIN;
    longStringReader.reset();
    receiverLogin = null;
    value = null;
  }
}
