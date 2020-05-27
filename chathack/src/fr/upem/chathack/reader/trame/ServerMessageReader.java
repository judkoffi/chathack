package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.publicframe.ServerResponseMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.LongSizedStringReader;

/**
 * Class use to read ServerMessage type
 *
 */
public class ServerMessageReader implements IReader<ServerResponseMessage> {
  private enum State {
    WAITING_MSG_TYPE, WAITING_MSG, DONE, ERROR
  }

  private final LongSizedStringReader reader;
  private State state;
  private ServerResponseMessage value;
  private boolean errorMessage;

  public ServerMessageReader() {
    this.reader = new LongSizedStringReader();
    this.state = State.WAITING_MSG_TYPE;
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_MSG_TYPE: {
        bb.flip();
        if (!bb.hasRemaining()) {
          bb.compact();
          return ProcessStatus.REFILL;
        }
        var msgType = bb.get();
        bb.compact();
        switch (msgType) {
          case OpCode.SERVER_ERROR_RESPONSE_TYPE:
            errorMessage = true;
            break;
          case OpCode.SERVER_NOT_ERROR_RESPONSE_TYPE:
            errorMessage = false;
            break;
          default:
            throw new IllegalArgumentException("unknown opcode " + msgType);
        }
        state = State.WAITING_MSG;
      }

      case WAITING_MSG: {
        var status = reader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        value = new ServerResponseMessage(reader.get(), errorMessage);
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public ServerResponseMessage get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_MSG_TYPE;
    reader.reset();
    value = null;
    errorMessage = false;
  }
}
