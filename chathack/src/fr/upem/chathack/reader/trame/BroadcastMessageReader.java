package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.publicframe.BroadcastMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.MessageReader;
/**
 * Class use to read BroadcastMessage type
 *
 */
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
        throw new IllegalStateException("illegal state: " + state);
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
