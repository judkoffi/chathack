package fr.upem.chathack.frame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.OpCode;
import fr.upem.chathack.common.reader.IReader;

public class ClientFrameReader implements IReader<IFrame> {

  private enum State {
    WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
  }

  /**
   * Readers
   */
  private final ServerMessageReader serverMessageReader;

  public ClientFrameReader() {
    this.serverMessageReader = new ServerMessageReader();
    this.state = State.WAITING_OPCODE;
  }

  private State state;
  private IReader<? extends IFrame> currentFrameReader;
  private IFrame value;

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_OPCODE: {
        bb.flip();
        if (!bb.hasRemaining()) {
          bb.compact();
          return ProcessStatus.REFILL;
        }
        var opcode = bb.get();
        bb.compact();
        switch (opcode) {
          case OpCode.SERVER_ERROR_MESSAGE:
            currentFrameReader = serverMessageReader;
            break;
          default:
            throw new IllegalArgumentException("unknow opcode " + opcode);
        }
        state = State.WAITING_CONTENT;
      }

      case WAITING_CONTENT: {
        var status = currentFrameReader.process(bb);
        if (status != ProcessStatus.DONE)
          return status;
        value = currentFrameReader.get();
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public IFrame get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_OPCODE;
    value = null;
    serverMessageReader.reset();
  }
}
