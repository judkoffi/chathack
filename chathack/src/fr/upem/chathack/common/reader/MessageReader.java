
package fr.upem.chathack.common.reader;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.Message;

public class MessageReader implements IReader<Message> {
  private Message message;

  private enum State {
    WAITING_LOGIN, WAITING_MESSAGE, DONE, ERROR
  }

  private State state = State.WAITING_LOGIN;
  private final StringReader stringReader = new StringReader();
  private String login;

  private ProcessStatus processPart(ByteBuffer bb) {
    var sizeStatus = stringReader.process(bb);
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

        login = stringReader.get();
        state = State.WAITING_MESSAGE;
        stringReader.reset();
      }

      case WAITING_MESSAGE: {
        var status = processPart(bb);
        if (status != ProcessStatus.DONE)
          return status;

        state = State.DONE;
        var msg = stringReader.get();
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
    stringReader.reset();
    message = null;
  }

}
