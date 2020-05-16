package fr.upem.chathack.common.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.Message;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.LongSizedStringReader;
import fr.upem.chathack.frame.BroadcastMessage;

public class BroadcastMessageReader implements IReader<BroadcastMessage> {

  private enum State {
    WAITING_LOGIN, WAITING_MESSAGE, DONE, ERROR
  }

  private final LongSizedStringReader reader;
  private State state;
  private BroadcastMessage value;
  private LongSizedString login;

  public BroadcastMessageReader() {
    this.reader = new LongSizedStringReader();
    this.state = State.WAITING_LOGIN;
  }


  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_LOGIN: {
        var status = reader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        login = reader.get();
        reader.reset();
        state = State.WAITING_MESSAGE;
      }
      case WAITING_MESSAGE: {
        var status = reader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        var password = reader.get();
        value = new BroadcastMessage(new Message(login, password));
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
    	  System.out.println(state);
        throw new IllegalStateException();
    }
  }

  @Override
  public BroadcastMessage get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_LOGIN;
    reader.reset();
    value = null;
  }

}
