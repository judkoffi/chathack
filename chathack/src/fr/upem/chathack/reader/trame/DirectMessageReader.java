package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.privateframe.DirectMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.LongSizedStringReader;
import fr.upem.chathack.reader.MessageReader;
/**
 * Class use to read DirectMessage type
 *
 */
public class DirectMessageReader implements IReader<DirectMessage> {
  private enum State {
    WAITING_TARGET, WAITING_MESSAGE, DONE, ERROR
  }

  private State state;
  private final LongSizedStringReader targetReader;
  private final MessageReader messageReader;
  private LongSizedString target;
  private DirectMessage message;

  public DirectMessageReader() {
    this.targetReader = new LongSizedStringReader();
    this.messageReader = new MessageReader();
    this.state = State.WAITING_TARGET;
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_TARGET: {
        var status = targetReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        target = targetReader.get();
        targetReader.reset();
        state = State.WAITING_MESSAGE;
      }
      case WAITING_MESSAGE: {
        var status = messageReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        var content = messageReader.get();
        message = new DirectMessage(target, content);
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public DirectMessage get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return message;
  }

  @Override
  public void reset() {
    state = State.WAITING_TARGET;
    targetReader.reset();
    messageReader.reset();
    target = null;
    message = null;
  }
}
