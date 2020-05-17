package fr.upem.chathack.common.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.Message;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.LongSizedStringReader;
import fr.upem.chathack.common.reader.MessageReader;
import fr.upem.chathack.frame.BroadcastMessage;

public class BroadcastMessageReader implements IReader<BroadcastMessage> {

  private enum State {
   WAITING_MESSAGE, DONE, ERROR
  }

  private final MessageReader reader;
  private State state;
  private BroadcastMessage value;

  public BroadcastMessageReader() {
    this.reader = new MessageReader();
    this.state = State.WAITING_MESSAGE;
  }


  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_MESSAGE: {
        var status = reader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        var message = reader.get();
        value = new BroadcastMessage(message);
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
    state = State.WAITING_MESSAGE;
    reader.reset();
    value = null;
  }

}
