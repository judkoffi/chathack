package fr.upem.chathack.common.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.OpCode;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.frame.IFrame;

public class ServerFrameReader implements IReader<IFrame> {

  private enum State {
    WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
  }

  /**
   * Readers
   */
  private final BroadcastMessageReader broadcastMessageReader;
  private final AnonymousConnectionReader anonymousReader;
  private final AuthentificatedConnectionReader authenticatedReader;

  private State state;
  private IReader<? extends IFrame> currentFrameReader;
  private IFrame value;


  public ServerFrameReader() {
    this.broadcastMessageReader = new BroadcastMessageReader();
    this.anonymousReader = new AnonymousConnectionReader();
    this.authenticatedReader = new AuthentificatedConnectionReader();
    this.state = State.WAITING_OPCODE;
  }

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
          case OpCode.ANONYMOUS_CLIENT_CONNECTION:
            System.out.println("anonymousReader");
            currentFrameReader = anonymousReader;
            break;
          case OpCode.AUTHENTICATED_CLIENT_CONNECTION:
            currentFrameReader = authenticatedReader;
            break;
          case OpCode.BROADCAST_MESSAGE:
            currentFrameReader = broadcastMessageReader;
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
    broadcastMessageReader.reset();
    authenticatedReader.reset();
    anonymousReader.reset();
  }
}
