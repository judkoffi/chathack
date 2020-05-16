package fr.upem.chathack.common.reader;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.Message;

public class LongMessageReader implements IReader<Message> {
  private enum State {
    WAITING_LOGIN, WAITING_MESSAGE, DONE, ERROR
  }

  private Message message;
  private State state = State.WAITING_LOGIN;
  private final StringLongReader stringLongReader = new StringLongReader();
  private String login;

  private ProcessStatus processPart(ByteBuffer bb) {
    var sizeStatus = stringLongReader.process(bb);
    switch (sizeStatus) {
      case ERROR:
        state = State.ERROR;
        return ProcessStatus.ERROR;
      case REFILL:
        return ProcessStatus.REFILL;
      case DONE:
        return ProcessStatus.DONE;
      default:
        throw new AssertionError();
    }
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_LOGIN: {
        var status = processPart(bb);
        if (status != ProcessStatus.DONE)
          return status;
        login = stringLongReader.get();
        state = State.WAITING_MESSAGE;
        stringLongReader.reset();
      }

      case WAITING_MESSAGE: {
        var status = processPart(bb);
        if (status != ProcessStatus.DONE)
          return status;

        state = State.DONE;
        var msg = stringLongReader.get();
        message = new Message(login, msg);
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public Message get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return message;
  }

  @Override
  public void reset() {
    state = State.WAITING_LOGIN;
    stringLongReader.reset();
    message = null;
  }
}
